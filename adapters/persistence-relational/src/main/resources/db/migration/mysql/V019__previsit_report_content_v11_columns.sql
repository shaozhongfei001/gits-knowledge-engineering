-- V019: Add v1.1 columns to previsit_report_content
-- Replaces flat customer_id/rm_id/visit_objective/content_json with structured fields

ALTER TABLE previsit_report_content ADD COLUMN IF NOT EXISTS customer_name VARCHAR(256);
ALTER TABLE previsit_report_content ADD COLUMN IF NOT EXISTS rm_name VARCHAR(256);
ALTER TABLE previsit_report_content ADD COLUMN IF NOT EXISTS customer_overview JSON;
ALTER TABLE previsit_report_content ADD COLUMN IF NOT EXISTS kyc_gap_summary JSON;
ALTER TABLE previsit_report_content ADD COLUMN IF NOT EXISTS product_schemes JSON;
ALTER TABLE previsit_report_content ADD COLUMN IF NOT EXISTS key_questions JSON;
ALTER TABLE previsit_report_content ADD COLUMN IF NOT EXISTS risk_reminders JSON;
ALTER TABLE previsit_report_content ADD COLUMN IF NOT EXISTS visit_strategy VARCHAR(2048);
