CREATE TABLE operating_case (
    case_id CHAR(36) PRIMARY KEY,
    case_type VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    purpose VARCHAR(512) NOT NULL,
    valid_from TIMESTAMP(6) NOT NULL,
    valid_to TIMESTAMP(6) NULL,
    recorded_at TIMESTAMP(6) NOT NULL,
    record_version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(128) NOT NULL,
    CONSTRAINT ck_case_valid_time CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

CREATE TABLE interaction (
    interaction_id CHAR(36) PRIMARY KEY,
    case_id CHAR(36) NOT NULL,
    occurred_at TIMESTAMP(6) NOT NULL,
    channel VARCHAR(64) NOT NULL,
    source_uri VARCHAR(1024) NOT NULL,
    source_version VARCHAR(128) NOT NULL,
    source_hash CHAR(64) NOT NULL,
    recorded_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_interaction_case FOREIGN KEY (case_id) REFERENCES operating_case(case_id)
);

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

CREATE TABLE claim_evidence (
    claim_id CHAR(36) NOT NULL,
    evidence_id CHAR(36) NOT NULL,
    relation_type VARCHAR(16) NOT NULL,
    PRIMARY KEY (claim_id, evidence_id, relation_type),
    CONSTRAINT fk_claim_evidence_claim FOREIGN KEY (claim_id) REFERENCES claim(claim_id),
    CONSTRAINT fk_claim_evidence_evidence FOREIGN KEY (evidence_id) REFERENCES evidence(evidence_id),
    CONSTRAINT ck_claim_evidence_relation CHECK (relation_type IN ('SUPPORTS', 'REFUTES', 'QUALIFIES', 'EXPLAINS'))
);

CREATE TABLE human_confirmation (
    confirmation_id CHAR(36) PRIMARY KEY,
    subject_type VARCHAR(32) NOT NULL,
    subject_id CHAR(36) NOT NULL,
    decision VARCHAR(32) NOT NULL,
    actor_id VARCHAR(128) NOT NULL,
    actor_role VARCHAR(128) NOT NULL,
    permission_decision_id VARCHAR(128) NOT NULL,
    confirmed_at TIMESTAMP(6) NOT NULL,
    comment_text VARCHAR(2000) NULL
);

CREATE TABLE controlled_action (
    action_id CHAR(36) PRIMARY KEY,
    proposal_id CHAR(36) NOT NULL,
    confirmation_id CHAR(36) NOT NULL,
    target_system VARCHAR(128) NOT NULL,
    target_object_type VARCHAR(128) NOT NULL,
    target_object_id VARCHAR(256) NOT NULL,
    expected_version VARCHAR(128) NOT NULL,
    operation VARCHAR(64) NOT NULL,
    payload_json VARCHAR(4000) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    requested_at TIMESTAMP(6) NOT NULL,
    UNIQUE KEY uk_action_idempotency (target_system, idempotency_key),
    CONSTRAINT fk_action_confirmation FOREIGN KEY (confirmation_id) REFERENCES human_confirmation(confirmation_id)
);

CREATE TABLE action_receipt (
    receipt_id CHAR(36) PRIMARY KEY,
    action_id CHAR(36) NOT NULL,
    status VARCHAR(32) NOT NULL,
    target_version_after VARCHAR(128) NULL,
    failure_code VARCHAR(128) NULL,
    raw_receipt_hash CHAR(64) NULL,
    received_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_receipt_action FOREIGN KEY (action_id) REFERENCES controlled_action(action_id)
);

CREATE TABLE evaluation_run (
    evaluation_id CHAR(36) PRIMARY KEY,
    run_manifest_id CHAR(36) NOT NULL,
    case_set_version VARCHAR(128) NOT NULL,
    gate_state VARCHAR(64) NOT NULL,
    metrics_json VARCHAR(4000) NOT NULL,
    evaluated_at TIMESTAMP(6) NOT NULL
);

CREATE TABLE outbox_event (
    event_id CHAR(36) PRIMARY KEY,
    aggregate_type VARCHAR(128) NOT NULL,
    aggregate_id VARCHAR(256) NOT NULL,
    event_type VARCHAR(256) NOT NULL,
    event_version VARCHAR(32) NOT NULL,
    payload_json VARCHAR(4000) NOT NULL,
    occurred_at TIMESTAMP(6) NOT NULL,
    published_at TIMESTAMP(6) NULL,
    publish_attempts INT NOT NULL DEFAULT 0,
    last_error_code VARCHAR(128) NULL
);

CREATE INDEX ix_claim_case_recorded ON claim(case_id, recorded_at);
CREATE INDEX ix_evidence_hash ON evidence(content_hash);
CREATE INDEX ix_outbox_unpublished ON outbox_event(published_at, occurred_at);
