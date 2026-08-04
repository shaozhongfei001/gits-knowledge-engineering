-- V003: Customer context tables
-- H2/MySQL compatible DDL
-- Business: customer profile, group structure, banking relationship, credit, transactions

-- ── 客户主档：对公客户基本信息 ──
CREATE TABLE IF NOT EXISTS customer (
    customer_id VARCHAR(36) PRIMARY KEY,
    customer_name VARCHAR(200) NOT NULL,
    customer_short_name VARCHAR(100),
    unified_social_credit_code VARCHAR(50),
    established_date DATE,
    registered_capital_cny BIGINT,
    industry VARCHAR(200),
    region VARCHAR(100),
    enterprise_scale VARCHAR(100),
    customer_tier VARCHAR(50),
    relationship_since DATE,
    rm_id VARCHAR(36) NOT NULL,
    rm_name VARCHAR(100),
    managing_branch VARCHAR(200),
    group_flag BOOLEAN DEFAULT FALSE,
    listed_status VARCHAR(50),
    risk_level VARCHAR(50),
    main_products CLOB,
    core_tags CLOB,
    relationship_summary CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- ── 法人实体：集团内关联法人主体 ──
CREATE TABLE IF NOT EXISTS legal_entity (
    entity_id VARCHAR(36) PRIMARY KEY,
    group_id VARCHAR(36) NOT NULL,
    name VARCHAR(200) NOT NULL,
    role VARCHAR(200),
    ownership VARCHAR(100),
    bank_customer_id VARCHAR(36),
    relationship_status VARCHAR(100),
    evidence_ref VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── 集团关系：法人实体间的股权/控制关系 ──
CREATE TABLE IF NOT EXISTS group_relationship (
    id VARCHAR(36) PRIMARY KEY,
    group_id VARCHAR(36) NOT NULL,
    from_entity_id VARCHAR(36) NOT NULL,
    to_entity_id VARCHAR(36) NOT NULL,
    relationship_type VARCHAR(50) NOT NULL,
    ownership_ratio INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── 银行关系快照：客户月度银行业务汇总 ──
CREATE TABLE IF NOT EXISTS bank_relationship_snapshot (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    snapshot_month VARCHAR(7) NOT NULL,
    avg_daily_deposit_cny BIGINT,
    monthly_settlement_cny BIGINT,
    loan_balance_cny BIGINT,
    credit_total_cny BIGINT,
    used_credit_cny BIGINT,
    available_credit_cny BIGINT,
    bank_acceptance_bill_balance_cny BIGINT,
    guarantee_balance_cny BIGINT,
    payroll_employees INTEGER,
    cash_management_opened BOOLEAN DEFAULT FALSE,
    supply_chain_finance_opened BOOLEAN DEFAULT FALSE,
    cross_border_settlement_cny BIGINT,
    product_count INTEGER,
    customer_contribution_level VARCHAR(10),
    anomaly_flags VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── 授信额度：客户综合授信信息 ──
CREATE TABLE IF NOT EXISTS credit_facility (
    facility_id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    borrower_entity VARCHAR(200) NOT NULL,
    approval_date DATE,
    maturity_date DATE,
    credit_total_cny BIGINT,
    used_credit_cny BIGINT,
    available_credit_cny BIGINT,
    current_loan_balance_cny BIGINT,
    bank_acceptance_bill_balance_cny BIGINT,
    guarantee_balance_cny BIGINT,
    collateral CLOB,
    purpose_allowed CLOB,
    purpose_restrictions CLOB,
    covenants CLOB,
    reconciliation_note CLOB,
    evidence_ref VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- ── 交易流水：客户交易明细记录 ──
CREATE TABLE IF NOT EXISTS transaction_ledger (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    transaction_date DATE NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    counterparty VARCHAR(200),
    amount_cny BIGINT NOT NULL,
    description CLOB,
    evidence_ref VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS ix_customer_rm ON customer(rm_id);
CREATE INDEX IF NOT EXISTS ix_legal_entity_group ON legal_entity(group_id);
CREATE INDEX IF NOT EXISTS ix_group_rel_group ON group_relationship(group_id);
CREATE INDEX IF NOT EXISTS ix_bank_snapshot_customer_month ON bank_relationship_snapshot(customer_id, snapshot_month);
CREATE INDEX IF NOT EXISTS ix_credit_facility_customer ON credit_facility(customer_id);
CREATE INDEX IF NOT EXISTS ix_transaction_customer_date ON transaction_ledger(customer_id, transaction_date);
