ALTER TABLE ai_requests
    ADD COLUMN request_hash BYTEA NOT NULL DEFAULT decode('', 'hex');

ALTER TABLE ai_requests
    ALTER COLUMN request_hash DROP DEFAULT;
