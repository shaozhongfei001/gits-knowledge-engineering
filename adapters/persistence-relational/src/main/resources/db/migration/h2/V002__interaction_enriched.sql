-- V002: Interaction enrichment + customer journey scenario tables
-- H2-compatible DDL (no AFTER, no ALTER COLUMN DROP DEFAULT)
-- Business: M17→M22 full interaction chain

-- ── Interaction table: drop-and-recreate with full 14-field schema ──
-- V001 created a 5-column stub; H2 doesn't support AFTER, so we recreate.
-- Data loss is acceptable at dev stage (V001 had no production data).

DROP TABLE IF EXISTS claim_evidence;
DROP TABLE IF EXISTS evidence;
DROP TABLE IF EXISTS claim;
DROP TABLE IF EXISTS interaction;

CREATE TABLE interaction (
    interaction_id CHAR(36) PRIMARY KEY,
    case_id CHAR(36) NOT NULL,
    journey_id CHAR(36) NULL,
    interaction_type VARCHAR(64) NOT NULL,
    direction VARCHAR(16) NOT NULL,
    channel VARCHAR(64) NOT NULL,
    content_summary VARCHAR(2000) NULL,
    outcome VARCHAR(32) NOT NULL,
    initiator_id VARCHAR(128) NOT NULL,
    initiator_role VARCHAR(64) NOT NULL,
    initiator_display_name VARCHAR(256) NOT NULL,
    produced_claim_ids VARCHAR(4000) NULL,
    occurred_at TIMESTAMP(6) NOT NULL,
    ended_at TIMESTAMP(6) NULL,
    source_uri VARCHAR(1024) NOT NULL DEFAULT '',
    source_version VARCHAR(128) NOT NULL DEFAULT '',
    source_hash CHAR(64) NOT NULL,
    recorded_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX ix_interaction_case_type ON interaction(case_id, interaction_type);
CREATE INDEX ix_interaction_journey ON interaction(journey_id);
CREATE INDEX ix_interaction_initiator ON interaction(initiator_role);
CREATE INDEX ix_interaction_outcome ON interaction(outcome);

-- Interaction participants (many-to-many)
CREATE TABLE interaction_participant (
    interaction_id CHAR(36) NOT NULL,
    participant_id VARCHAR(128) NOT NULL,
    participant_role VARCHAR(64) NOT NULL,
    display_name VARCHAR(256) NOT NULL,
    PRIMARY KEY (interaction_id, participant_id),
    CONSTRAINT fk_participant_interaction FOREIGN KEY (interaction_id) REFERENCES interaction(interaction_id),
    CONSTRAINT ck_participant_role CHECK (participant_role IN ('AI_AGENT', 'RELATIONSHIP_MANAGER', 'COMPLIANCE_OFFICER', 'PRODUCT_SPECIALIST', 'CUSTOMER', 'SYSTEM'))
);

-- ── Re-create claim and evidence tables (dropped above for FK order) ──
CREATE TABLE claim (
    claim_id CHAR(36) PRIMARY KEY,
    case_id CHAR(36) NOT NULL,
    interaction_id CHAR(36) NULL,
    claim_type VARCHAR(64) NOT NULL,
    claim_status VARCHAR(32) NOT NULL,
    statement_text TEXT NOT NULL,
    valid_from TIMESTAMP(6) NULL,
    valid_to TIMESTAMP(6) NULL,
    recorded_at TIMESTAMP(6) NOT NULL,
    supersedes_claim_id CHAR(36) NULL,
    model_run_id CHAR(36) NULL,
    CONSTRAINT fk_claim_case FOREIGN KEY (case_id) REFERENCES operating_case(case_id),
    CONSTRAINT fk_claim_interaction FOREIGN KEY (interaction_id) REFERENCES interaction(interaction_id),
    CONSTRAINT fk_claim_supersedes FOREIGN KEY (supersedes_claim_id) REFERENCES claim(claim_id),
    CONSTRAINT ck_claim_valid_time CHECK (valid_to IS NULL OR valid_from IS NULL OR valid_to >= valid_from)
);

CREATE INDEX ix_claim_case_recorded ON claim(case_id, recorded_at);

CREATE TABLE evidence (
    evidence_id CHAR(36) PRIMARY KEY,
    source_uri VARCHAR(1024) NOT NULL,
    source_version VARCHAR(128) NOT NULL,
    locator VARCHAR(1024) NOT NULL,
    content_hash CHAR(64) NOT NULL,
    permission_label VARCHAR(64) NOT NULL,
    license_ref VARCHAR(256) NULL,
    recorded_at TIMESTAMP(6) NOT NULL
);

CREATE INDEX ix_evidence_hash ON evidence(content_hash);

CREATE TABLE claim_evidence (
    claim_id CHAR(36) NOT NULL,
    evidence_id CHAR(36) NOT NULL,
    relation_type VARCHAR(16) NOT NULL,
    PRIMARY KEY (claim_id, evidence_id, relation_type),
    CONSTRAINT fk_claim_evidence_claim FOREIGN KEY (claim_id) REFERENCES claim(claim_id),
    CONSTRAINT fk_claim_evidence_evidence FOREIGN KEY (evidence_id) REFERENCES evidence(evidence_id),
    CONSTRAINT ck_claim_evidence_relation CHECK (relation_type IN ('SUPPORTS', 'REFUTES', 'QUALIFIES', 'EXPLAINS'))
);

-- ── Customer Journey scenario tables (M17–M22) ──

CREATE TABLE customer_journey (
    journey_id CHAR(36) PRIMARY KEY,
    case_id CHAR(36) NOT NULL,
    customer_id VARCHAR(128) NOT NULL,
    customer_name VARCHAR(256) NOT NULL,
    phase VARCHAR(32) NOT NULL,
    started_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NULL,
    CONSTRAINT fk_journey_case FOREIGN KEY (case_id) REFERENCES operating_case(case_id)
);

CREATE TABLE insight_claim (
    insight_id CHAR(36) PRIMARY KEY,
    claim_id CHAR(36) NOT NULL,
    operating_case_id CHAR(36) NOT NULL,
    insight_category VARCHAR(64) NOT NULL,
    insight_summary VARCHAR(2000) NOT NULL,
    generated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_insight_claim FOREIGN KEY (claim_id) REFERENCES claim(claim_id),
    CONSTRAINT fk_insight_case FOREIGN KEY (operating_case_id) REFERENCES operating_case(case_id)
);

CREATE TABLE product_candidate_claim (
    product_id CHAR(36) PRIMARY KEY,
    claim_id CHAR(36) NOT NULL,
    insight_claim_id CHAR(36) NOT NULL,
    operating_case_id CHAR(36) NOT NULL,
    product_code VARCHAR(64) NOT NULL,
    product_name VARCHAR(256) NOT NULL,
    match_reason VARCHAR(2000) NOT NULL,
    proposed_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_product_insight FOREIGN KEY (insight_claim_id) REFERENCES insight_claim(insight_id),
    CONSTRAINT fk_product_case FOREIGN KEY (operating_case_id) REFERENCES operating_case(case_id),
    CONSTRAINT fk_product_claim FOREIGN KEY (claim_id) REFERENCES claim(claim_id)
);

CREATE TABLE previsit_report (
    report_id CHAR(36) PRIMARY KEY,
    operating_case_id CHAR(36) NOT NULL,
    journey_id CHAR(36) NOT NULL,
    insight_ids VARCHAR(4000) NOT NULL,
    product_candidate_ids VARCHAR(4000) NOT NULL,
    summary VARCHAR(2000) NOT NULL,
    generated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_report_case FOREIGN KEY (operating_case_id) REFERENCES operating_case(case_id),
    CONSTRAINT fk_report_journey FOREIGN KEY (journey_id) REFERENCES customer_journey(journey_id)
);

CREATE TABLE postvisit_analysis (
    analysis_id CHAR(36) PRIMARY KEY,
    operating_case_id CHAR(36) NOT NULL,
    journey_id CHAR(36) NOT NULL,
    previsit_report_id CHAR(36) NOT NULL,
    outcome VARCHAR(2000) NOT NULL,
    follow_up_action VARCHAR(2000) NULL,
    analyzed_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_analysis_case FOREIGN KEY (operating_case_id) REFERENCES operating_case(case_id),
    CONSTRAINT fk_analysis_report FOREIGN KEY (previsit_report_id) REFERENCES previsit_report(report_id)
);
