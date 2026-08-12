CREATE TABLE ai_usage (
    installation_hash BYTEA NOT NULL,
    usage_date DATE NOT NULL,
    used INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (installation_hash, usage_date),
    CONSTRAINT ai_usage_non_negative CHECK (used >= 0)
);

CREATE TABLE ai_requests (
    installation_hash BYTEA NOT NULL,
    message_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (installation_hash, message_id)
);

CREATE INDEX ai_requests_created_at_idx ON ai_requests (created_at);