#!/bin/sh

#
# Copyright 2026 Stanislav Aleshin
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -eu

if [ -n "${DATABASE_APP_PASSWORD_FILE:-}" ]; then
    DATABASE_APP_PASSWORD="$(cat "$DATABASE_APP_PASSWORD_FILE")"
fi

if [ -n "${DATABASE_MIGRATOR_PASSWORD_FILE:-}" ]; then
    DATABASE_MIGRATOR_PASSWORD="$(cat "$DATABASE_MIGRATOR_PASSWORD_FILE")"
fi

: "${DATABASE_APP_PASSWORD:?DATABASE_APP_PASSWORD or DATABASE_APP_PASSWORD_FILE is required}"
: "${DATABASE_MIGRATOR_PASSWORD:?DATABASE_MIGRATOR_PASSWORD or DATABASE_MIGRATOR_PASSWORD_FILE is required}"

export DATABASE_APP_PASSWORD DATABASE_MIGRATOR_PASSWORD

psql \
    --username "$POSTGRES_USER" \
    --dbname "$POSTGRES_DB" \
    --set=ON_ERROR_STOP=1 <<'EOSQL'

\getenv database_name POSTGRES_DB
\getenv app_password DATABASE_APP_PASSWORD
\getenv migrator_password DATABASE_MIGRATOR_PASSWORD

REVOKE ALL
    ON DATABASE :"database_name"
    FROM PUBLIC;

REVOKE CREATE ON SCHEMA public FROM PUBLIC;

CREATE ROLE studyassistant_migrator
    LOGIN
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    CONNECTION LIMIT 5
    PASSWORD :'migrator_password';

CREATE ROLE studyassistant_app
    LOGIN
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    CONNECTION LIMIT 20
    PASSWORD :'app_password';

GRANT CONNECT
    ON DATABASE :"database_name"
    TO studyassistant_migrator;

GRANT CONNECT
    ON DATABASE :"database_name"
    TO studyassistant_app;

GRANT USAGE, CREATE
    ON SCHEMA public
    TO studyassistant_migrator;

GRANT USAGE
    ON SCHEMA public
    TO studyassistant_app;

ALTER DEFAULT PRIVILEGES
    FOR ROLE studyassistant_migrator
    IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE
    ON TABLES
    TO studyassistant_app;

ALTER DEFAULT PRIVILEGES
    FOR ROLE studyassistant_migrator
    IN SCHEMA public
    GRANT USAGE, SELECT
    ON SEQUENCES
    TO studyassistant_app;

ALTER ROLE studyassistant_app SET statement_timeout = '125s';
ALTER ROLE studyassistant_app SET idle_in_transaction_session_timeout = '30s';
ALTER ROLE studyassistant_app SET search_path = public, pg_catalog;

ALTER ROLE studyassistant_migrator SET statement_timeout = '5min';
ALTER ROLE studyassistant_migrator SET idle_in_transaction_session_timeout = '1min';
ALTER ROLE studyassistant_migrator SET search_path = public, pg_catalog;

EOSQL
