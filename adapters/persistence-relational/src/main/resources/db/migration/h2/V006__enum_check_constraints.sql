-- V006: Add CHECK constraints for enum-typed columns (H2 compatible)
-- Aligns SQL constraints with Java enum values

-- operating_case.case_type
ALTER TABLE operating_case ADD CONSTRAINT ck_case_type
    CHECK (case_type IN ('CONTINUOUS_ENGAGEMENT', 'CLAIM_RECONCILIATION'));

-- claim.claim_type
ALTER TABLE claim ADD CONSTRAINT ck_claim_type
    CHECK (claim_type IN ('CUSTOMER_JOURNEY', 'OPPORTUNITY', 'PRODUCT_CANDIDATE', 'CUSTOMER_STATEMENT', 'SYSTEM_FACT', 'RISK_SIGNAL', 'COMMITMENT', 'FOLLOW_UP'));

-- interaction.channel
ALTER TABLE interaction ADD CONSTRAINT ck_channel
    CHECK (channel IN ('PHONE', 'IN_PERSON', 'EMAIL', 'INSTANT_MESSAGE', 'VIDEO_CONFERENCE', 'SYSTEM_PUSH', 'CRM_PUSH', 'RISK_SIGNAL_ENGINE', 'AI_INSIGHT_ENGINE', 'PRODUCT_MATCH_ENGINE', 'FACE_TO_FACE', 'PHONE_CALL'));

-- customer.industry
ALTER TABLE customer ADD CONSTRAINT ck_industry
    CHECK (industry IN ('MANUFACTURING', 'FINANCE', 'TECHNOLOGY', 'REAL_ESTATE', 'ENERGY', 'HEALTHCARE', 'AGRICULTURE', 'LOGISTICS', 'RETAIL', 'OTHER'));

-- customer.enterprise_scale
ALTER TABLE customer ADD CONSTRAINT ck_enterprise_scale
    CHECK (enterprise_scale IN ('LARGE', 'MEDIUM', 'SMALL', 'MICRO'));

-- customer.customer_tier
ALTER TABLE customer ADD CONSTRAINT ck_customer_tier
    CHECK (customer_tier IN ('STRATEGIC', 'KEY', 'GROWTH', 'GENERAL'));

-- customer.listed_status
ALTER TABLE customer ADD CONSTRAINT ck_listed_status
    CHECK (listed_status IN ('LISTED', 'UNLISTED', 'DELISTED'));

-- customer.risk_level
ALTER TABLE customer ADD CONSTRAINT ck_risk_level
    CHECK (risk_level IN ('HIGH', 'MEDIUM', 'LOW'));

-- Missing index for customer.rm_id
CREATE INDEX IF NOT EXISTS ix_customer_rm ON customer(rm_id);
