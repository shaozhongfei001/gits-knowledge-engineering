-- ═══════════════════════════════════════════════════════════════════
-- V015: V1.1 Human Gate + Audit Trace tables
-- H2-compatible version (MySQL compatibility mode)
-- ═══════════════════════════════════════════════════════════════════

-- 人工门禁表
CREATE TABLE IF NOT EXISTS human_gate (
    gate_id CHAR(36) PRIMARY KEY,
    gate_type VARCHAR(64) NOT NULL,
    journey_id CHAR(36) NULL,
    customer_id CHAR(36) NULL,
    operating_case_id CHAR(36) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    subject VARCHAR(512) NOT NULL,
    proposal JSON NULL,
    evidence_refs JSON NULL,
    decision VARCHAR(32) NULL,
    modification JSON NULL,
    decision_reason TEXT NULL,
    actor_id VARCHAR(128) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    decided_at TIMESTAMP(6) NULL
);

-- CRM写回命令表
CREATE TABLE IF NOT EXISTS crm_writeback_command (
    command_id CHAR(36) PRIMARY KEY,
    journey_id CHAR(36) NULL,
    customer_id CHAR(36) NULL,
    operating_case_id CHAR(36) NULL,
    operation VARCHAR(64) NOT NULL,
    target_entity VARCHAR(128) NOT NULL,
    payload JSON NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    human_confirmation_required BOOLEAN NOT NULL DEFAULT TRUE,
    decision VARCHAR(32) NULL,
    modifications JSON NULL,
    decision_reason TEXT NULL,
    actor_id VARCHAR(128) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    decided_at TIMESTAMP(6) NULL,
    sent_at TIMESTAMP(6) NULL,
    error_message TEXT NULL
);

-- 审计追踪表
CREATE TABLE IF NOT EXISTS audit_trace (
    trace_id CHAR(36) PRIMARY KEY,
    entity_type VARCHAR(64) NOT NULL,
    entity_id CHAR(36) NOT NULL,
    operation VARCHAR(64) NOT NULL,
    before_snapshot JSON NULL,
    after_snapshot JSON NULL,
    actor_id VARCHAR(128) NULL,
    actor_role VARCHAR(64) NULL,
    occurred_at TIMESTAMP(6) NOT NULL,
    correlation_id CHAR(36) NULL
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_hg_status ON human_gate (status);
CREATE INDEX IF NOT EXISTS idx_hg_gate_type ON human_gate (gate_type);
CREATE INDEX IF NOT EXISTS idx_hg_journey ON human_gate (journey_id);
CREATE INDEX IF NOT EXISTS idx_hg_customer ON human_gate (customer_id);
CREATE INDEX IF NOT EXISTS idx_crm_status ON crm_writeback_command (status);
CREATE INDEX IF NOT EXISTS idx_crm_journey ON crm_writeback_command (journey_id);
CREATE INDEX IF NOT EXISTS idx_crm_customer ON crm_writeback_command (customer_id);
CREATE INDEX IF NOT EXISTS idx_at_entity ON audit_trace (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_at_actor ON audit_trace (actor_id);
CREATE INDEX IF NOT EXISTS idx_at_occurred ON audit_trace (occurred_at);
CREATE INDEX IF NOT EXISTS idx_at_correlation ON audit_trace (correlation_id);
