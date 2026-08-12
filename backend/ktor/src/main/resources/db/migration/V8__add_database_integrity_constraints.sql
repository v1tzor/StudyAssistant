ALTER TABLE rate_limit_events
    ADD CONSTRAINT rate_limit_events_installation_hash_size
        CHECK (octet_length(installation_hash) = 32);

ALTER TABLE schedule_shares
    ADD CONSTRAINT schedule_shares_code_hash_size
        CHECK (octet_length(code_hash) = 32),
    ADD CONSTRAINT schedule_shares_creator_hash_size
        CHECK (octet_length(creator_hash) = 32),
    ADD CONSTRAINT schedule_shares_payload_size
        CHECK (octet_length(payload) BETWEEN 17 AND 1048592),
    ADD CONSTRAINT schedule_shares_nonce_size
        CHECK (octet_length(payload_nonce) = 12),
    ADD CONSTRAINT schedule_shares_claim_hash_size
        CHECK (claim_hash IS NULL OR octet_length(claim_hash) = 32),
    ADD CONSTRAINT schedule_shares_claim_consistency
        CHECK ((claim_hash IS NULL) = (claimed_until IS NULL)),
    ADD CONSTRAINT schedule_shares_consumed_claim
        CHECK (consumed_at IS NULL OR claim_hash IS NOT NULL);

ALTER TABLE homework_shares
    ADD CONSTRAINT homework_shares_code_hash_size
        CHECK (octet_length(code_hash) = 32),
    ADD CONSTRAINT homework_shares_creator_hash_size
        CHECK (octet_length(creator_hash) = 32),
    ADD CONSTRAINT homework_shares_payload_size
        CHECK (octet_length(payload) BETWEEN 17 AND 1048592),
    ADD CONSTRAINT homework_shares_nonce_size
        CHECK (octet_length(payload_nonce) = 12);

ALTER TABLE ai_usage
    ADD CONSTRAINT ai_usage_installation_hash_size
        CHECK (octet_length(installation_hash) = 32);

ALTER TABLE ai_requests
    ADD CONSTRAINT ai_requests_installation_hash_size
        CHECK (octet_length(installation_hash) = 32),
    ADD CONSTRAINT ai_requests_request_hash_size
        CHECK (octet_length(request_hash) IN (0, 32)),
    ADD CONSTRAINT ai_requests_in_flight_execution_count
        CHECK (in_flight <= execution_count);
