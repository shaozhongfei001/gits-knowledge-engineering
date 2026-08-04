-- V007: PostvisitAnalysisContent + PrevisitReportContent persistence tables (H2 compatible)
-- P9 Loop G1+G2 gap-fill: persist analysis content for context inheritance

CREATE TABLE IF NOT EXISTS postvisit_analysis_content (
    id                  VARCHAR(36)   NOT NULL PRIMARY KEY,
    analysis_id         VARCHAR(36)   NOT NULL UNIQUE,
    journey_id          VARCHAR(36)   NOT NULL,
    operating_case_id   VARCHAR(36)   NOT NULL,
    visit_summary       CLOB,
    key_findings_json           CLOB,
    opportunity_signals_json    CLOB,
    commitments_json            CLOB,
    reconciliation_items_json   CLOB,
    follow_up_actions_json      CLOB,
    next_step_recommendation    CLOB,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pac_journey ON postvisit_analysis_content (journey_id);
CREATE INDEX idx_pac_case    ON postvisit_analysis_content (operating_case_id);

CREATE TABLE IF NOT EXISTS previsit_report_content (
    id                  VARCHAR(36)   NOT NULL PRIMARY KEY,
    report_id           VARCHAR(36)   NOT NULL UNIQUE,
    journey_id          VARCHAR(36)   NOT NULL,
    operating_case_id   VARCHAR(36)   NOT NULL,
    customer_id         VARCHAR(36)   NOT NULL,
    rm_id               VARCHAR(36),
    visit_objective     CLOB,
    content_json        CLOB,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_prc_journey ON previsit_report_content (journey_id);
CREATE INDEX idx_prc_case    ON previsit_report_content (operating_case_id);
