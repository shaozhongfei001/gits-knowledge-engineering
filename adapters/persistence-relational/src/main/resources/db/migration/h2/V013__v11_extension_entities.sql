-- ═══════════════════════════════════════════════════════════════════
-- V013: V1.1 extension entities (version chain, lifecycle, interaction extension)
-- H2-compatible version (MySQL compatibility mode)
-- ═══════════════════════════════════════════════════════════════════

-- 证据版本链接表
CREATE TABLE IF NOT EXISTS evidence_version_link (
    link_id CHAR(36) PRIMARY KEY,
    evidence_id CHAR(36) NOT NULL,
    previous_version_id CHAR(36) NULL,
    next_version_id CHAR(36) NULL,
    version_number INT NOT NULL,
    change_type VARCHAR(32) NOT NULL,
    change_reason TEXT NULL,
    changed_by VARCHAR(128) NULL,
    changed_at TIMESTAMP(6) NOT NULL
);

-- 声明生命周期事件表
CREATE TABLE IF NOT EXISTS claim_lifecycle_event (
    event_id CHAR(36) PRIMARY KEY,
    claim_id CHAR(36) NOT NULL,
    from_status VARCHAR(32) NULL,
    to_status VARCHAR(32) NOT NULL,
    transition_reason TEXT NULL,
    actor_id VARCHAR(128) NULL,
    actor_role VARCHAR(64) NULL,
    transitioned_at TIMESTAMP(6) NOT NULL
);

-- 交互扩展表
CREATE TABLE IF NOT EXISTS interaction_extension (
    extension_id CHAR(36) PRIMARY KEY,
    interaction_id CHAR(36) NOT NULL,
    recording_consent_id CHAR(36) NULL,
    commitment_ids JSON NULL,
    task_ids JSON NULL,
    opportunity_ids JSON NULL,
    kyc_gap_profile_id CHAR(36) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT UQ_ie_interaction UNIQUE (interaction_id)
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_evl_evidence ON evidence_version_link (evidence_id);
CREATE INDEX IF NOT EXISTS idx_evl_previous ON evidence_version_link (previous_version_id);
CREATE INDEX IF NOT EXISTS idx_cle_claim ON claim_lifecycle_event (claim_id);
CREATE INDEX IF NOT EXISTS idx_cle_actor ON claim_lifecycle_event (actor_id);
CREATE INDEX IF NOT EXISTS idx_ie_consent ON interaction_extension (recording_consent_id);
CREATE INDEX IF NOT EXISTS idx_ie_kyc ON interaction_extension (kyc_gap_profile_id);
