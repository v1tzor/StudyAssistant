#!/bin/sh

set -eu

docker compose \
    --env-file /etc/studyassistant/production.env \
    -f /opt/studyassistant/backend/ktor/compose.production.yml \
    exec -T nginx \
    nginx -s reload
