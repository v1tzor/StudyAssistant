CREATE TABLE ad_reward_challenges (
    id UUID PRIMARY KEY,
    installation_hash BYTEA NOT NULL,
    purpose VARCHAR(40) NOT NULL,
    subject_hash BYTEA,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    consumed_at TIMESTAMPTZ,
    CONSTRAINT ad_reward_challenges_installation_hash_size
        CHECK (octet_length(installation_hash) = 32),
    CONSTRAINT ad_reward_challenges_subject_hash_size
        CHECK (subject_hash IS NULL OR octet_length(subject_hash) = 32),
    CONSTRAINT ad_reward_challenges_purpose
        CHECK (purpose IN ('ai_quota_reset', 'schedule_import')),
    CONSTRAINT ad_reward_challenges_subject
        CHECK ((purpose = 'schedule_import') = (subject_hash IS NOT NULL)),
    CONSTRAINT ad_reward_challenges_consumption
        CHECK (consumed_at IS NULL OR completed_at IS NOT NULL)
);

CREATE INDEX ad_reward_challenges_installation_idx
    ON ad_reward_challenges (installation_hash, purpose, created_at DESC);

CREATE INDEX ad_reward_challenges_schedule_idx
    ON ad_reward_challenges (installation_hash, subject_hash)
    WHERE purpose = 'schedule_import' AND completed_at IS NOT NULL;

CREATE TABLE ai_reward_grants (
    challenge_id UUID PRIMARY KEY REFERENCES ad_reward_challenges(id) ON DELETE CASCADE,
    installation_hash BYTEA NOT NULL,
    usage_date DATE NOT NULL,
    amount INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ai_reward_grants_installation_hash_size
        CHECK (octet_length(installation_hash) = 32),
    CONSTRAINT ai_reward_grants_amount_positive CHECK (amount > 0)
);

CREATE INDEX ai_reward_grants_installation_date_idx
    ON ai_reward_grants (installation_hash, usage_date);
