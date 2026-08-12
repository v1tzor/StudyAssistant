ALTER TABLE ai_requests
    ADD COLUMN usage_date DATE,
    ADD COLUMN in_flight INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN succeeded BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN updated_at TIMESTAMPTZ;

UPDATE ai_requests
SET usage_date = (created_at AT TIME ZONE 'UTC')::DATE,
    updated_at = created_at;

ALTER TABLE ai_requests
    ALTER COLUMN usage_date SET NOT NULL,
    ALTER COLUMN succeeded SET DEFAULT FALSE,
    ALTER COLUMN updated_at SET NOT NULL,
    ALTER COLUMN updated_at SET DEFAULT NOW(),
    ADD CONSTRAINT ai_requests_in_flight_non_negative CHECK (in_flight >= 0);

CREATE INDEX ai_requests_updated_at_idx ON ai_requests (updated_at);
