-- V004: Knowledge rules tables (H2 compatible)
-- Business: product definitions, compliance rules, external events

-- ── 产品目录：银行产品定义与准入条件 ──
CREATE TABLE IF NOT EXISTS product_catalog (
    product_id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    definition CLOB,
    key_conditions CLOB,
    required_materials CLOB,
    risk_points CLOB,
    trigger VARCHAR(500),
    prohibited_phrases CLOB,
    evidence_source VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── 政策规则：合规与风控规则引擎 ──
CREATE TABLE IF NOT EXISTS policy_rule (
    rule_id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    logic CLOB NOT NULL,
    required_output CLOB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_policy_rule_severity CHECK (severity IN ('CRITICAL', 'HIGH', 'MEDIUM'))
);

-- ── 外部事件：舆情与市场事件追踪 ──
CREATE TABLE IF NOT EXISTS external_event (
    event_id VARCHAR(36) PRIMARY KEY,
    event_date DATE NOT NULL,
    source_type VARCHAR(100),
    source_name VARCHAR(200),
    entity VARCHAR(200),
    title VARCHAR(500) NOT NULL,
    content CLOB,
    confidence VARCHAR(20),
    reliability VARCHAR(20),
    bank_use_allowed BOOLEAN DEFAULT TRUE,
    linked_themes CLOB,
    possible_business_signal VARCHAR(500),
    no_go_statement CLOB,
    evidence_ref VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_external_event_confidence CHECK (confidence IS NULL OR confidence IN ('HIGH', 'MEDIUM', 'LOW')),
    CONSTRAINT ck_external_event_reliability CHECK (reliability IS NULL OR reliability IN ('VERIFIED', 'UNVERIFIED', 'DISPUTED'))
);

CREATE INDEX IF NOT EXISTS ix_product_catalog_name ON product_catalog(name);
CREATE INDEX IF NOT EXISTS ix_policy_rule_severity ON policy_rule(severity);
CREATE INDEX IF NOT EXISTS ix_external_event_date ON external_event(event_date);
CREATE INDEX IF NOT EXISTS ix_external_event_entity ON external_event(entity);
