CREATE UNIQUE INDEX schedule_shares_claim_hash_idx
    ON schedule_shares (claim_hash)
    WHERE claim_hash IS NOT NULL;