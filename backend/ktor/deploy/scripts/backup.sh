#!/bin/sh

set -eu

DEFAULT_COMPOSE_FILE="/opt/studyassistant/backend/ktor/compose.production.yml"

COMPOSE_FILE="${COMPOSE_FILE:-$DEFAULT_COMPOSE_FILE}"
ENV_FILE="${ENV_FILE:?ENV_FILE is required}"
BACKUP_DIRECTORY="${BACKUP_DIRECTORY:-/var/backups/studyassistant}"
RETENTION_DAYS="${RETENTION_DAYS:-14}"
BACKUP_LOCK_FILE="${BACKUP_LOCK_FILE:-/run/lock/studyassistant-backup.lock}"

exec 9>"$BACKUP_LOCK_FILE"
if ! flock -n 9; then
    echo "Another backup is already running" >&2
    exit 1
fi

case "$BACKUP_DIRECTORY" in
    /*) ;;
    *) echo "BACKUP_DIRECTORY must be absolute" >&2; exit 1 ;;
esac

if [ "$BACKUP_DIRECTORY" = "/" ]; then
    echo "BACKUP_DIRECTORY must not be the filesystem root" >&2
    exit 1
fi

case "$RETENTION_DAYS" in
    ''|*[!0-9]*) echo "RETENTION_DAYS must be a positive integer" >&2; exit 1 ;;
esac

if [ "$RETENTION_DAYS" -lt 1 ]; then
    echo "RETENTION_DAYS must be at least one" >&2
    exit 1
fi

umask 077
mkdir -p "$BACKUP_DIRECTORY"

TIMESTAMP="$(date -u +%Y%m%dT%H%M%SZ)"
BACKUP_FILE="$BACKUP_DIRECTORY/studyassistant-$TIMESTAMP.dump"
CHECKSUM_FILE="$BACKUP_FILE.sha256"
TEMPORARY_FILE="$(mktemp "$BACKUP_DIRECTORY/.studyassistant-backup.XXXXXX")"

cleanup() {
    rm -f -- "$TEMPORARY_FILE"
}

trap cleanup EXIT HUP INT TERM

docker compose \
    --env-file "$ENV_FILE" \
    -f "$COMPOSE_FILE" \
    exec -T postgres \
    sh -c 'exec pg_dump --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" --format=custom --no-owner --exclude-table-data=public.schedule_shares --exclude-table-data=public.homework_shares' \
    > "$TEMPORARY_FILE"

docker compose \
    --env-file "$ENV_FILE" \
    -f "$COMPOSE_FILE" \
    exec -T postgres \
    pg_restore --list \
    < "$TEMPORARY_FILE" \
    > /dev/null

mv -- "$TEMPORARY_FILE" "$BACKUP_FILE"
sha256sum "$BACKUP_FILE" > "$CHECKSUM_FILE"

find "$BACKUP_DIRECTORY" \
    -type f \
    \( -name 'studyassistant-*.dump' -o -name 'studyassistant-*.dump.sha256' \) \
    -mtime "+$RETENTION_DAYS" \
    -delete

trap - EXIT HUP INT TERM

echo "Backup completed: $BACKUP_FILE"
