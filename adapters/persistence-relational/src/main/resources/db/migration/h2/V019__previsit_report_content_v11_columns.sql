-- V019: Add v1.1 columns to previsit_report_content
-- Replaces flat customer_id/rm_id/visit_objective/content_json with structured fields

-- Change id from VARCHAR(36) NOT NULL to BIGINT AUTO_INCREMENT
ALTER TABLE previsit_report_content ALTER COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE previsit_report_content ADD COLUMN IF NOT EXISTS customer_name VARCHAR(256);
ALTER TABLE previsit_report_content ADD COLUMN IF NOT EXISTS rm_name VARCHAR(256);
ALTER TABLE previsit_report_content ADD COLUMN IF NOT EXISTS customer_overview CLOB;
ALTER TABLE previsit_report_content ADD COLUMN IF NOT EXISTS kyc_gap_summary CLOB;
ALTER TABLE previsit_report_content ADD COLUMN IF NOT EXISTS product_schemes CLOB;
ALTER TABLE previsit_report_content ADD COLUMN IF NOT EXISTS key_questions CLOB;
ALTER TABLE previsit_report_content ADD COLUMN IF NOT EXISTS risk_reminders CLOB;
ALTER TABLE previsit_report_content ADD COLUMN IF NOT EXISTS visit_strategy VARCHAR(2048);
