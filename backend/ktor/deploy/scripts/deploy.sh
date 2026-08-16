#!/bin/sh

set -eu

SCRIPT_DIRECTORY="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
COMPOSE_FILE="${COMPOSE_FILE:-/opt/studyassistant/backend/ktor/compose.production.yml}"
ENV_FILE="${ENV_FILE:-/etc/studyassistant/production.env}"
API_URL="${API_URL:-https://api.studyassistant-app.ru}"
DEPLOY_LOCK_FILE="${DEPLOY_LOCK_FILE:-/run/lock/studyassistant-deploy.lock}"

exec 9>"$DEPLOY_LOCK_FILE"
if ! flock -n 9; then
    echo "Another deployment or rollback is already running" >&2
    exit 1
fi

ENV_FILE="$ENV_FILE" "$SCRIPT_DIRECTORY/preflight.sh"
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" config --quiet

BACKEND_IMAGE="$(sed -n 's/^BACKEND_IMAGE=//p' "$ENV_FILE")"
cosign verify \
    --certificate-identity-regexp '^https://github.com/v1tzor/StudyAssistant/.github/workflows/backend-container.yml@refs/tags/backend-v[-A-Za-z0-9._]+$' \
    --certificate-oidc-issuer 'https://token.actions.githubusercontent.com' \
    "$BACKEND_IMAGE" \
    > /dev/null

POSTGRES_CONTAINER="$(
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps --status running -q postgres
)"

if [ -n "$POSTGRES_CONTAINER" ] && [ "${SKIP_BACKUP:-0}" != "1" ]; then
    ENV_FILE="$ENV_FILE" \
        COMPOSE_FILE="$COMPOSE_FILE" \
        "$SCRIPT_DIRECTORY/backup.sh"
fi

docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" pull backend xray

docker compose \
    --env-file "$ENV_FILE" \
    -f "$COMPOSE_FILE" \
    up --detach --remove-orphans --wait --no-build --force-recreate nginx

API_URL="$API_URL" "$SCRIPT_DIRECTORY/healthcheck.sh"

echo "Deployment completed"
