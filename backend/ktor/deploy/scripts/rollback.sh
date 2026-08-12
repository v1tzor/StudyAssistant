#!/bin/sh

set -eu

SCRIPT_DIRECTORY="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
COMPOSE_FILE="${COMPOSE_FILE:-/opt/studyassistant/backend/ktor/compose.production.yml}"
ENV_FILE="${ENV_FILE:-/etc/studyassistant/production.env}"
API_URL="${API_URL:-https://api.studyassistant-app.ru}"
PREVIOUS_BACKEND_IMAGE="${PREVIOUS_BACKEND_IMAGE:?PREVIOUS_BACKEND_IMAGE is required}"
DEPLOY_LOCK_FILE="${DEPLOY_LOCK_FILE:-/run/lock/studyassistant-deploy.lock}"

exec 9>"$DEPLOY_LOCK_FILE"
if ! flock -n 9; then
    echo "Another deployment or rollback is already running" >&2
    exit 1
fi

if ! printf '%s\n' "$PREVIOUS_BACKEND_IMAGE" | grep -Eq '^[-A-Za-z0-9._/:]+@sha256:[0-9a-f]{64}$'; then
    echo "PREVIOUS_BACKEND_IMAGE must use an immutable sha256 digest" >&2
    exit 1
fi

ENV_FILE="$ENV_FILE" "$SCRIPT_DIRECTORY/preflight.sh"

cosign verify \
    --certificate-identity-regexp '^https://github.com/v1tzor/StudyAssistant/.github/workflows/backend-container.yml@refs/tags/backend-v[-A-Za-z0-9._]+$' \
    --certificate-oidc-issuer 'https://token.actions.githubusercontent.com' \
    "$PREVIOUS_BACKEND_IMAGE" \
    > /dev/null

docker pull "$PREVIOUS_BACKEND_IMAGE" > /dev/null

BACKEND_IMAGE="$PREVIOUS_BACKEND_IMAGE" docker compose \
    --env-file "$ENV_FILE" \
    -f "$COMPOSE_FILE" \
    up --detach --no-deps --no-build --wait backend

API_URL="$API_URL" "$SCRIPT_DIRECTORY/healthcheck.sh"

echo "Application rollback completed with image: $PREVIOUS_BACKEND_IMAGE"
echo "Persist BACKEND_IMAGE=$PREVIOUS_BACKEND_IMAGE in $ENV_FILE after verification"
