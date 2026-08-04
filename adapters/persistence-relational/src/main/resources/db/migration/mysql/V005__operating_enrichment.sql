-- V005: Operating enrichment tables
-- H2/MySQL compatible DDL
-- Business: KYC gap analysis, fact reconciliation, opportunity signals, commitments, relationship reports

-- ── KYC缺口画像：客户信息完备度评估 ──
CREATE TABLE IF NOT EXISTS kyc_gap_profile (
    profile_id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    as_of DATE NOT NULL,
    known_items CLOB,
    partial_known_items CLOB,
    stale_items CLOB,
    conflicting_or_ambiguous_items CLOB,
    unknown_items CLOB,
    priority_questions CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- ── 事实核查案例：多源事实冲突的裁决记录 ──
CREATE TABLE IF NOT EXISTS fact_reconciliation_case (
    reconciliation_id VARCHAR(36) PRIMARY KEY,
    case_id VARCHAR(36) NOT NULL,
    topic VARCHAR(200) NOT NULL,
    structured_fact CLOB,
    interaction_claim CLOB,
    external_fact CLOB,
    ontology_distinction CLOB,
    correct_judgment CLOB NOT NULL,
    wrong_output_examples CLOB,
    next_action CLOB,
    `status` VARCHAR(50) DEFAULT 'OPEN',
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_reconciliation_case FOREIGN KEY (case_id) REFERENCES operating_case(case_id),
    CONSTRAINT ck_fact_rec_status CHECK (`status` IN ('OPEN', 'RESOLVED', 'ESCALATED'))
);

-- ── 机会信号：业务机会识别与追踪 ──
CREATE TABLE IF NOT EXISTS opportunity_signal (
    signal_id VARCHAR(36) PRIMARY KEY,
    operating_case_id VARCHAR(36) NOT NULL,
    journey_id VARCHAR(36),
    signal_type VARCHAR(50) NOT NULL,
    content CLOB NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    source_ref VARCHAR(100),
    confidence DECIMAL(5,2),
    `status` VARCHAR(50) DEFAULT 'DETECTED',
    evidence_ref VARCHAR(100),
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_signal_case FOREIGN KEY (operating_case_id) REFERENCES operating_case(case_id),
    CONSTRAINT ck_signal_type CHECK (signal_type IN ('FINANCING_NEED', 'PRODUCT_OPPORTUNITY', 'RELATIONSHIP_CHANGE')),
    CONSTRAINT ck_signal_source CHECK (source_type IN ('INTERACTION', 'EXTERNAL_EVENT', 'ANALYSIS')),
    CONSTRAINT ck_signal_status CHECK (`status` IN ('DETECTED', 'CONFIRMED', 'DISMISSED', 'CONVERTED'))
);

-- ── 承诺跟踪：客户/银行双方承诺管理 ──
CREATE TABLE IF NOT EXISTS commitment (
    commitment_id VARCHAR(36) PRIMARY KEY,
    operating_case_id VARCHAR(36) NOT NULL,
    journey_id VARCHAR(36),
    commitment_type VARCHAR(50) NOT NULL,
    content CLOB NOT NULL,
    owner VARCHAR(100) NOT NULL,
    due_date DATE,
    `status` VARCHAR(50) DEFAULT 'OPEN',
    evidence_ref VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fulfilled_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_commitment_case FOREIGN KEY (operating_case_id) REFERENCES operating_case(case_id),
    CONSTRAINT ck_commitment_type CHECK (commitment_type IN ('CUSTOMER_COMMITMENT', 'BANK_COMMITMENT')),
    CONSTRAINT ck_commitment_status CHECK (`status` IN ('OPEN', 'FULFILLED', 'OVERDUE', 'CANCELLED'))
);

-- ── 关系报告：客户关系分析与拜访报告 ──
CREATE TABLE IF NOT EXISTS relationship_report (
    report_id VARCHAR(36) PRIMARY KEY,
    operating_case_id VARCHAR(36) NOT NULL,
    journey_id VARCHAR(36),
    report_type VARCHAR(50) NOT NULL,
    content CLOB NOT NULL,
    based_on_evidence CLOB,
    based_on_reconciliations CLOB,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    supersedes_report_id VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_report_case FOREIGN KEY (operating_case_id) REFERENCES operating_case(case_id),
    CONSTRAINT fk_report_supersedes FOREIGN KEY (supersedes_report_id) REFERENCES relationship_report(report_id),
    CONSTRAINT ck_report_type CHECK (report_type IN ('INTERNAL_RELATIONSHIP', 'CRM_CALL', 'UPDATED_RELATIONSHIP', 'NEXT_PREVISIT'))
);

CREATE INDEX IF NOT EXISTS ix_kyc_gap_customer ON kyc_gap_profile(customer_id);
CREATE INDEX IF NOT EXISTS ix_fact_rec_case ON fact_reconciliation_case(case_id);
CREATE INDEX IF NOT EXISTS ix_opportunity_case ON opportunity_signal(operating_case_id);
CREATE INDEX IF NOT EXISTS ix_opportunity_journey ON opportunity_signal(journey_id);
CREATE INDEX IF NOT EXISTS ix_opportunity_status ON opportunity_signal(`status`);
CREATE INDEX IF NOT EXISTS ix_commitment_case ON commitment(operating_case_id);
CREATE INDEX IF NOT EXISTS ix_commitment_journey ON commitment(journey_id);
CREATE INDEX IF NOT EXISTS ix_commitment_status ON commitment(`status`);
CREATE INDEX IF NOT EXISTS ix_report_case ON relationship_report(operating_case_id);
CREATE INDEX IF NOT EXISTS ix_report_journey ON relationship_report(journey_id);
