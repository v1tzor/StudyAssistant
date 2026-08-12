#!/bin/sh

set -eu

API_URL="${API_URL:-https://api.studyassistant-app.ru}"

curl \
    --fail \
    --silent \
    --show-error \
    --connect-timeout 5 \
    --max-time 10 \
    "$API_URL/health/ready" \
    > /dev/null

echo "Backend is ready: $API_URL"
