CREATE TABLE rate_limit_events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    installation_hash BYTEA NOT NULL,
    type VARCHAR(40) NOT NULL,
    amount INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT rate_limit_events_amount_positive CHECK (amount > 0)
);

CREATE INDEX rate_limit_events_lookup_idx ON rate_limit_events (installation_hash, type, created_at);
CREATE INDEX rate_limit_events_created_at_idx ON rate_limit_events (created_at);