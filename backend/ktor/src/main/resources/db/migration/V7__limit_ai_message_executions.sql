ALTER TABLE ai_requests
    ADD COLUMN execution_count INTEGER NOT NULL DEFAULT 1,
    ADD CONSTRAINT ai_requests_execution_count_positive CHECK (execution_count > 0);

ALTER TABLE ai_requests
    ALTER COLUMN execution_count DROP DEFAULT;
