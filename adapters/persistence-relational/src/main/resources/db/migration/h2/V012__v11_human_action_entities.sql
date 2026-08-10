-- ═══════════════════════════════════════════════════════════════════
-- V012: V1.1 human-action new entities
-- H2-compatible version (MySQL compatibility mode)
-- ═══════════════════════════════════════════════════════════════════

-- 录音录像同意表
CREATE TABLE IF NOT EXISTS recording_consent (
    consent_id CHAR(36) PRIMARY KEY,
    interaction_id CHAR(36) NOT NULL,
    customer_id CHAR(36) NULL,
    consent_type VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    granted_by VARCHAR(128) NULL,
    granted_role VARCHAR(64) NULL,
    granted_at TIMESTAMP(6) NOT NULL,
    withdrawal_reason TEXT NULL,
    expires_at TIMESTAMP(6) NULL,
    legal_basis VARCHAR(256) NULL
);

-- 任务表
CREATE TABLE IF NOT EXISTS task (
    task_id CHAR(36) PRIMARY KEY,
    interaction_id CHAR(36) NULL,
    customer_id CHAR(36) NULL,
    operating_case_id CHAR(36) NULL,
    task_type VARCHAR(64) NOT NULL,
    title VARCHAR(512) NOT NULL,
    description TEXT NULL,
    status VARCHAR(32) NOT NULL,
    priority VARCHAR(32) NULL,
    assigned_to VARCHAR(128) NULL,
    assigned_role VARCHAR(64) NULL,
    due_date VARCHAR(32) NULL,
    completed_date VARCHAR(32) NULL,
    tags JSON NULL,
    parent_task_id CHAR(36) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_consent_interaction ON recording_consent (interaction_id);
CREATE INDEX IF NOT EXISTS idx_consent_customer ON recording_consent (customer_id);
CREATE INDEX IF NOT EXISTS idx_consent_status ON recording_consent (status);
CREATE INDEX IF NOT EXISTS idx_task_interaction ON task (interaction_id);
CREATE INDEX IF NOT EXISTS idx_task_customer ON task (customer_id);
CREATE INDEX IF NOT EXISTS idx_task_status ON task (status);
CREATE INDEX IF NOT EXISTS idx_task_assigned ON task (assigned_to);
CREATE INDEX IF NOT EXISTS idx_task_parent ON task (parent_task_id);
