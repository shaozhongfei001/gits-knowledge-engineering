-- ═══════════════════════════════════════════════════════════════════
-- V010: Production indexes for common query patterns
-- H2-compatible version (MySQL compatibility mode)
-- ═══════════════════════════════════════════════════════════════════

-- customer_journey: queries by customer_id and phase (no status column, uses phase)
CREATE INDEX idx_customer_journey_customer_id ON customer_journey (customer_id);
CREATE INDEX idx_customer_journey_phase ON customer_journey (phase);

-- interaction: queries by case_id and occurred_at (no customer_id column)
CREATE INDEX idx_interaction_case_id ON interaction (case_id);
CREATE INDEX idx_interaction_occurred_at ON interaction (occurred_at);

-- claim: queries by case_id and claim_status (status column is claim_status)
CREATE INDEX idx_claim_case_id ON claim (case_id);
CREATE INDEX idx_claim_claim_status ON claim (claim_status);

-- operating_case: queries by case_type and status
CREATE INDEX idx_operating_case_case_type ON operating_case (case_type);
CREATE INDEX idx_operating_case_status ON operating_case (status);

-- outreach_script: queries by customer_id (already has idx_outreach_customer)
-- meeting_script: queries by customer_id (already has idx_meeting_customer)
-- postvisit_analysis_content: queries by operating_case_id (already has idx_pac_case)
-- previsit_report_content: queries by operating_case_id (already has idx_prc_case)
-- transaction: queries by customer_id and transaction_date (already has idx_txn_customer, idx_txn_date)

-- Additional indexes for scenario tables
CREATE INDEX idx_insight_claim_operating_case_id ON insight_claim (operating_case_id);
CREATE INDEX idx_product_candidate_claim_operating_case_id ON product_candidate_claim (operating_case_id);
CREATE INDEX idx_previsit_report_operating_case_id ON previsit_report (operating_case_id);
CREATE INDEX idx_postvisit_analysis_operating_case_id ON postvisit_analysis (operating_case_id);
