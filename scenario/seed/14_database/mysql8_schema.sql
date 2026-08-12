-- MySQL 8.0 reference schema for synthetic demo data.
-- Canonical truth remains JSON/CSV files in this package; this schema is a development adapter.

CREATE DATABASE IF NOT EXISTS rm_continuous_demo DEFAULT CHARACTER SET utf8mb4;
USE rm_continuous_demo;

CREATE TABLE legal_entity (
  entity_id VARCHAR(32) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  short_name VARCHAR(100),
  entity_type VARCHAR(32) NOT NULL,
  role_desc VARCHAR(255),
  ownership_parent VARCHAR(32),
  ownership_pct DECIMAL(8,2),
  bank_customer_id VARCHAR(32),
  status VARCHAR(32)
);

CREATE TABLE group_relationship (
  relation_id VARCHAR(32) PRIMARY KEY,
  from_entity VARCHAR(32) NOT NULL,
  to_entity VARCHAR(32) NOT NULL,
  relation_type VARCHAR(32) NOT NULL,
  ownership_pct DECIMAL(8,2),
  effective_from DATE,
  source_ref VARCHAR(128)
);

CREATE TABLE bank_account (
  account_id VARCHAR(32) PRIMARY KEY,
  entity_id VARCHAR(32) NOT NULL,
  account_type VARCHAR(64),
  currency VARCHAR(8),
  opened_date DATE,
  status VARCHAR(32),
  visibility_scope VARCHAR(64)
);

CREATE TABLE daily_balance (
  date DATE NOT NULL,
  account_id VARCHAR(32) NOT NULL,
  currency VARCHAR(8),
  eod_balance_cny DECIMAL(18,2),
  source_system VARCHAR(64),
  as_of DATE,
  visibility_scope VARCHAR(64),
  PRIMARY KEY(date, account_id)
);

CREATE TABLE bank_transaction (
  transaction_id VARCHAR(40) PRIMARY KEY,
  tx_date DATE NOT NULL,
  account_id VARCHAR(32) NOT NULL,
  counterparty_id VARCHAR(32),
  counterparty_name VARCHAR(255),
  counterparty_type VARCHAR(64),
  direction VARCHAR(8),
  amount_cny DECIMAL(18,2),
  purpose VARCHAR(255),
  channel VARCHAR(64),
  source_system VARCHAR(64),
  bank_visible_only BOOLEAN,
  evidence_eligible BOOLEAN,
  INDEX idx_tx_date (tx_date),
  INDEX idx_tx_account (account_id),
  INDEX idx_tx_cp_type (counterparty_type)
);

CREATE TABLE credit_facility (
  facility_id VARCHAR(32) PRIMARY KEY,
  borrower_entity_id VARCHAR(32),
  facility_type VARCHAR(128),
  approved_amount_cny DECIMAL(18,2),
  used_amount_cny DECIMAL(18,2),
  available_amount_cny DECIMAL(18,2),
  approval_date DATE,
  expiry_date DATE,
  allowed_purpose TEXT,
  restriction_desc TEXT,
  status VARCHAR(32)
);

CREATE TABLE external_event (
  event_id VARCHAR(32) PRIMARY KEY,
  event_date DATE,
  category VARCHAR(64),
  entity_name VARCHAR(255),
  title VARCHAR(255),
  content TEXT,
  relevance VARCHAR(16),
  reliability VARCHAR(16),
  evidence_type VARCHAR(32),
  source_name VARCHAR(255),
  opportunity_hint VARCHAR(255),
  no_go TEXT
);

CREATE TABLE interaction (
  interaction_id VARCHAR(40) PRIMARY KEY,
  interaction_date DATE,
  channel VARCHAR(32),
  source_mode VARCHAR(64),
  recording_consent VARCHAR(32),
  summary TEXT
);

CREATE TABLE interaction_object (
  object_id VARCHAR(40) PRIMARY KEY,
  interaction_id VARCHAR(40),
  object_type VARCHAR(64),
  content TEXT,
  speaker_id VARCHAR(32),
  object_status VARCHAR(64),
  crm_fact_allowed BOOLEAN,
  evidence_refs JSON
);

CREATE TABLE claim_assessment (
  assessment_id VARCHAR(40) PRIMARY KEY,
  topic VARCHAR(255),
  claim_ref VARCHAR(40),
  result VARCHAR(64),
  correct_interpretation TEXT,
  owner_id VARCHAR(32),
  payload JSON
);

CREATE TABLE commitment (
  commitment_id VARCHAR(40) PRIMARY KEY,
  side VARCHAR(16),
  owner_id VARCHAR(32),
  content TEXT,
  due_date DATE,
  status VARCHAR(32),
  source_ref VARCHAR(64)
);

CREATE TABLE task (
  task_id VARCHAR(40) PRIMARY KEY,
  owner_id VARCHAR(32),
  content TEXT,
  due_date DATE,
  related_commitment VARCHAR(40),
  status VARCHAR(32),
  task_type VARCHAR(64)
);

CREATE TABLE product_knowledge_version (
  product_id VARCHAR(32) NOT NULL,
  version VARCHAR(32) NOT NULL,
  name VARCHAR(255),
  effective_from DATE,
  effective_to DATE,
  status VARCHAR(32),
  owner_org VARCHAR(255),
  payload JSON,
  PRIMARY KEY(product_id, version)
);

CREATE TABLE skill_call (
  call_id VARCHAR(40) PRIMARY KEY,
  call_ts DATETIME(3),
  event_id VARCHAR(40),
  pain_point_refs JSON,
  skill_product VARCHAR(255),
  runtime_skills JSON,
  input_refs JSON,
  output_ref VARCHAR(255),
  human_gate_ref VARCHAR(40),
  status VARCHAR(32),
  latency_ms INT,
  side_effect VARCHAR(32)
);

CREATE TABLE human_gate_decision (
  gate_id VARCHAR(40) PRIMARY KEY,
  pain_point_id VARCHAR(16),
  event_id VARCHAR(40),
  decision_ts DATETIME(3),
  gate_name VARCHAR(255),
  decision VARCHAR(64),
  decided_by VARCHAR(32),
  decision_note TEXT,
  audit_ref VARCHAR(64)
);

CREATE TABLE crm_writeback_command (
  command_id VARCHAR(40) PRIMARY KEY,
  object_type VARCHAR(64),
  operation_type VARCHAR(32),
  proposed JSON,
  risk_level VARCHAR(16),
  human_gate VARCHAR(40),
  rm_decision VARCHAR(32),
  edit_to JSON,
  idempotency_key VARCHAR(64) UNIQUE
);

CREATE TABLE report_version (
  report_id VARCHAR(32),
  report_date DATE,
  version VARCHAR(32),
  file_path VARCHAR(255),
  based_on JSON,
  status VARCHAR(32),
  PRIMARY KEY(report_id, version)
);
