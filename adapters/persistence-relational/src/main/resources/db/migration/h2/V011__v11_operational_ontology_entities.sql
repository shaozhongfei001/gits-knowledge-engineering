-- ═══════════════════════════════════════════════════════════════════
-- V011: V1.1 扩展现有表 + 新增表
-- H2-compatible version (MySQL compatibility mode)
-- 注意: external_event, commitment, kyc_gap_profile 已在V004/V005中创建
-- ═══════════════════════════════════════════════════════════════════

-- 扩展 external_event 表 (V004创建)
ALTER TABLE external_event ADD COLUMN IF NOT EXISTS severity VARCHAR(32) DEFAULT 'MEDIUM';
ALTER TABLE external_event ADD COLUMN IF NOT EXISTS affected_industries JSON;
ALTER TABLE external_event ADD COLUMN IF NOT EXISTS affected_customer_ids JSON;
ALTER TABLE external_event ADD COLUMN IF NOT EXISTS detected_at TIMESTAMP(6);
ALTER TABLE external_event ADD COLUMN IF NOT EXISTS raw_payload TEXT;

-- 扩展 commitment 表 (V005创建)
ALTER TABLE commitment ADD COLUMN IF NOT EXISTS interaction_id CHAR(36);
ALTER TABLE commitment ADD COLUMN IF NOT EXISTS customer_id CHAR(36);
ALTER TABLE commitment ADD COLUMN IF NOT EXISTS fulfilled_date VARCHAR(32);
ALTER TABLE commitment ADD COLUMN IF NOT EXISTS assigned_to VARCHAR(128);
ALTER TABLE commitment ADD COLUMN IF NOT EXISTS verified_by VARCHAR(128);
ALTER TABLE commitment ADD COLUMN IF NOT EXISTS recorded_at TIMESTAMP(6);
ALTER TABLE commitment ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP(6);

-- 扩展 kyc_gap_profile 表 (V005创建)
ALTER TABLE kyc_gap_profile ADD COLUMN IF NOT EXISTS overall_completeness VARCHAR(16);
ALTER TABLE kyc_gap_profile ADD COLUMN IF NOT EXISTS risk_impact VARCHAR(32);
ALTER TABLE kyc_gap_profile ADD COLUMN IF NOT EXISTS last_assessed_by VARCHAR(128);
ALTER TABLE kyc_gap_profile ADD COLUMN IF NOT EXISTS last_assessed_at TIMESTAMP(6);

-- 新增产品知识版本表
CREATE TABLE IF NOT EXISTS product_knowledge_version (
    version_id CHAR(36) PRIMARY KEY,
    product_id CHAR(36) NOT NULL,
    version_number INT NOT NULL,
    product_name VARCHAR(256) NOT NULL,
    category VARCHAR(64) NOT NULL,
    description TEXT NULL,
    key_features JSON NULL,
    target_industries JSON NULL,
    risk_level VARCHAR(32) NULL,
    required_materials JSON NULL,
    pricing_basis VARCHAR(256) NULL,
    previous_version_id CHAR(36) NULL,
    change_summary TEXT NULL,
    changed_by VARCHAR(128) NULL,
    changed_at TIMESTAMP(6) NOT NULL
);

-- 新增商机表 (与V005的opportunity_signal不同)
CREATE TABLE IF NOT EXISTS opportunity (
    opportunity_id CHAR(36) PRIMARY KEY,
    customer_id CHAR(36) NOT NULL,
    interaction_id CHAR(36) NULL,
    operating_case_id CHAR(36) NULL,
    opportunity_type VARCHAR(64) NOT NULL,
    product_id CHAR(36) NULL,
    product_name VARCHAR(256) NULL,
    description TEXT NULL,
    status VARCHAR(32) NOT NULL,
    estimated_amount VARCHAR(64) NULL,
    probability VARCHAR(32) NULL,
    assigned_to VARCHAR(128) NULL,
    source VARCHAR(64) NULL,
    next_steps JSON NULL,
    expected_close_date VARCHAR(32) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

-- 新增索引
CREATE INDEX IF NOT EXISTS idx_pkv_product_id ON product_knowledge_version (product_id);
CREATE INDEX IF NOT EXISTS idx_pkv_category ON product_knowledge_version (category);
CREATE INDEX IF NOT EXISTS idx_opportunity_customer ON opportunity (customer_id);
CREATE INDEX IF NOT EXISTS idx_opportunity_status ON opportunity (status);
CREATE INDEX IF NOT EXISTS idx_opportunity_type ON opportunity (opportunity_type);
