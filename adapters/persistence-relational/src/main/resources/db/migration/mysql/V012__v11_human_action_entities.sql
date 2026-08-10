-- ═══════════════════════════════════════════════════════════════════
-- V012: V1.1 human-action new entities
-- MySQL version
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
    legal_basis VARCHAR(256) NULL,
    INDEX idx_consent_interaction (interaction_id),
    INDEX idx_consent_customer (customer_id),
    INDEX idx_consent_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
    updated_at TIMESTAMP(6) NOT NULL,
    INDEX idx_task_interaction (interaction_id),
    INDEX idx_task_customer (customer_id),
    INDEX idx_task_status (status),
    INDEX idx_task_assigned (assigned_to),
    INDEX idx_task_parent (parent_task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
