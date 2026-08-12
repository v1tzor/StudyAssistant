ALTER TABLE ai_requests
    ADD COLUMN last_execution_hash BYTEA NOT NULL DEFAULT decode('', 'hex'),
    ADD CONSTRAINT ai_requests_last_execution_hash_size
        CHECK (octet_length(last_execution_hash) IN (0, 32));

ALTER TABLE ai_requests
    ALTER COLUMN last_execution_hash DROP DEFAULT;

ALTER TABLE schedule_shares
    ADD COLUMN payload_size BIGINT
        GENERATED ALWAYS AS (octet_length(payload)::BIGINT) STORED,
    ADD CONSTRAINT schedule_shares_payload_size_positive
        CHECK (payload_size > 0);

ALTER TABLE homework_shares
    ADD COLUMN payload_size BIGINT
        GENERATED ALWAYS AS (octet_length(payload)::BIGINT) STORED,
    ADD CONSTRAINT homework_shares_payload_size_positive
        CHECK (payload_size > 0);
