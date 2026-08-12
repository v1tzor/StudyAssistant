CREATE TABLE schedule_shares (
    id UUID PRIMARY KEY,
    code_hash BYTEA NOT NULL UNIQUE,
    creator_hash BYTEA NOT NULL,
    item_count INTEGER NOT NULL,
    payload BYTEA NOT NULL,
    payload_nonce BYTEA NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    claim_hash BYTEA,
    claimed_until TIMESTAMPTZ,
    consumed_at TIMESTAMPTZ,
    CONSTRAINT schedule_shares_item_count CHECK (item_count BETWEEN 1 AND 100),
    CONSTRAINT schedule_shares_expiration CHECK (expires_at > created_at)
);

CREATE TABLE homework_shares(
    id UUID PRIMARY KEY,
    code_hash BYTEA NOT NULL UNIQUE,
    creator_hash BYTEA NOT NULL,
    item_count INTEGER NOT NULL,
    payload BYTEA NOT NULL,
    payload_nonce BYTEA NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT homework_shares_item_count CHECK (item_count BETWEEN 1 AND 20),
    CONSTRAINT homework_shares_expiration CHECK (expires_at > created_at)
);

CREATE INDEX schedule_shares_expires_at_idx ON schedule_shares (expires_at);
CREATE INDEX schedule_shares_creator_idx ON schedule_shares (creator_hash, created_at);

CREATE INDEX homework_shares_expires_at_idx ON homework_shares (expires_at);
CREATE INDEX homework_shares_creator_idx ON homework_shares (creator_hash, created_at);