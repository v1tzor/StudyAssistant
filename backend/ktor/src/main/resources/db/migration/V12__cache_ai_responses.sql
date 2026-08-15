ALTER TABLE ai_requests
    ADD COLUMN response_payload BYTEA,
    ADD COLUMN response_nonce BYTEA,
    ADD CONSTRAINT ai_requests_response_cache_consistency
        CHECK ((response_payload IS NULL) = (response_nonce IS NULL)),
    ADD CONSTRAINT ai_requests_response_payload_size
        CHECK (
            response_payload IS NULL OR
            octet_length(response_payload) BETWEEN 17 AND 1100016
        ),
    ADD CONSTRAINT ai_requests_response_nonce_size
        CHECK (response_nonce IS NULL OR octet_length(response_nonce) = 12);
