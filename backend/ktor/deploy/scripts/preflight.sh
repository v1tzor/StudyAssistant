#!/bin/sh

set -eu

RELEASE_DIRECTORY="${RELEASE_DIRECTORY:-/opt/studyassistant}"
ENV_FILE="${ENV_FILE:-/etc/studyassistant/production.env}"
SECRET_DIRECTORY="${SECRET_DIRECTORY:-/etc/studyassistant/secrets}"

for path in "$RELEASE_DIRECTORY" "$ENV_FILE" "$SECRET_DIRECTORY"; do
    case "$path" in
        /*) ;;
        *) echo "Deployment paths must be absolute" >&2; exit 1 ;;
    esac
done

if [ ! -d "$RELEASE_DIRECTORY" ] || [ ! -f "$ENV_FILE" ] || [ ! -d "$SECRET_DIRECTORY" ]; then
    echo "Deployment files are incomplete" >&2
    exit 1
fi

if [ -n "$(find "$RELEASE_DIRECTORY" -xdev ! -user root -print -quit)" ] ||
    [ -n "$(find "$RELEASE_DIRECTORY" -xdev -perm /022 -print -quit)" ]; then
    echo "Release tree must be root-owned and not group/other writable" >&2
    exit 1
fi

if [ -n "$(find "$ENV_FILE" ! -user root -print -quit)" ] ||
    [ -n "$(find "$ENV_FILE" -perm /077 -print -quit)" ]; then
    echo "Production environment file must be root-owned with mode 0600" >&2
    exit 1
fi

if [ "$(stat -c '%u:%g:%a' "$ENV_FILE")" != "0:0:600" ]; then
    echo "Production environment file must be root:root with mode 0600" >&2
    exit 1
fi

if [ -n "$(find "$SECRET_DIRECTORY" -xdev ! -user root -print -quit)" ] ||
    [ -n "$(find "$SECRET_DIRECTORY" -xdev -perm /027 -print -quit)" ] ||
    [ -n "$(find "$SECRET_DIRECTORY" -xdev -type f ! -gid 10001 -print -quit)" ]; then
    echo "Secret files must be root:10001, inaccessible to other users, and not group writable" >&2
    exit 1
fi

REQUIRED_SECRET_FILES="
postgres_admin_password
database_app_password
database_migrator_password
installation_hmac_secret
share_hmac_secret
payload_encryption_key
deepseek_api_key
openrouter_api_key
"

for secret_name in $REQUIRED_SECRET_FILES; do
    secret_path="$SECRET_DIRECTORY/$secret_name"
    if [ ! -f "$secret_path" ] || [ -L "$secret_path" ] || [ ! -s "$secret_path" ]; then
        echo "Required secret file is missing, empty, or not regular: $secret_name" >&2
        exit 1
    fi
    if [ "$(stat -c '%u:%g:%a' "$secret_path")" != "0:10001:640" ]; then
        echo "Secret file must be root:10001 with mode 0640: $secret_name" >&2
        exit 1
    fi
done

for secret_name in installation_hmac_secret share_hmac_secret payload_encryption_key; do
    if ! tr -d '\r\n' < "$SECRET_DIRECTORY/$secret_name" |
        grep -Eq '^[0-9a-fA-F]{64}$'; then
        echo "Cryptographic key must contain exactly 64 hexadecimal characters: $secret_name" >&2
        exit 1
    fi
done

require_env_file_value() {
    variable_name="$1"
    expected_value="$2"
    actual_value="$(sed -n "s/^${variable_name}=//p" "$ENV_FILE")"

    if [ "$actual_value" != "$expected_value" ]; then
        echo "$variable_name must point to the audited secret path" >&2
        exit 1
    fi
}

require_env_file_value POSTGRES_ADMIN_PASSWORD_FILE "$SECRET_DIRECTORY/postgres_admin_password"
require_env_file_value DATABASE_APP_PASSWORD_FILE "$SECRET_DIRECTORY/database_app_password"
require_env_file_value DATABASE_MIGRATOR_PASSWORD_FILE "$SECRET_DIRECTORY/database_migrator_password"
require_env_file_value INSTALLATION_HMAC_SECRET_FILE "$SECRET_DIRECTORY/installation_hmac_secret"
require_env_file_value SHARE_HMAC_SECRET_FILE "$SECRET_DIRECTORY/share_hmac_secret"
require_env_file_value PAYLOAD_ENCRYPTION_KEY_FILE "$SECRET_DIRECTORY/payload_encryption_key"
require_env_file_value DEEPSEEK_API_KEY_FILE "$SECRET_DIRECTORY/deepseek_api_key"
require_env_file_value OPENROUTER_API_KEY_FILE "$SECRET_DIRECTORY/openrouter_api_key"

BACKEND_IMAGE="$(sed -n 's/^BACKEND_IMAGE=//p' "$ENV_FILE")"
if ! printf '%s\n' "$BACKEND_IMAGE" | grep -Eq '^[-A-Za-z0-9._/:]+@sha256:[0-9a-f]{64}$'; then
    echo "BACKEND_IMAGE must use an immutable sha256 digest" >&2
    exit 1
fi

if ! command -v docker >/dev/null 2>&1 ||
    ! command -v cosign >/dev/null 2>&1 ||
    ! command -v flock >/dev/null 2>&1; then
    echo "docker, cosign, and flock are required" >&2
    exit 1
fi

echo "Deployment preflight passed"
