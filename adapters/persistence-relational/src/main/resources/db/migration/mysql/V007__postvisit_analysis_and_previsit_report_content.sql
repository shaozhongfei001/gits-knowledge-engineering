-- V007: PostvisitAnalysisContent + PrevisitReportContent persistence tables
-- P9 Loop G1+G2 gap-fill: persist analysis content for context inheritance

CREATE TABLE IF NOT EXISTS postvisit_analysis_content (
    id                  VARCHAR(36)   NOT NULL PRIMARY KEY,
    analysis_id         VARCHAR(36)   NOT NULL UNIQUE,
    journey_id          VARCHAR(36)   NOT NULL,
    operating_case_id   VARCHAR(36)   NOT NULL,
    visit_summary       TEXT,
    key_findings_json           TEXT COMMENT 'JSON-serialized List<InteractionExtraction>',
    opportunity_signals_json    TEXT COMMENT 'JSON-serialized List<OpportunitySignalItem>',
    commitments_json            TEXT COMMENT 'JSON-serialized List<CommitmentItem>',
    reconciliation_items_json   TEXT COMMENT 'JSON-serialized List<FactReconciliationItem>',
    follow_up_actions_json      TEXT COMMENT 'JSON-serialized List<String>',
    next_step_recommendation    TEXT,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_pac_journey (journey_id),
    INDEX idx_pac_case   (operating_case_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS previsit_report_content (
    id                  VARCHAR(36)   NOT NULL PRIMARY KEY,
    report_id           VARCHAR(36)   NOT NULL UNIQUE,
    journey_id          VARCHAR(36)   NOT NULL,
    operating_case_id   VARCHAR(36)   NOT NULL,
    customer_id         VARCHAR(36)   NOT NULL,
    rm_id               VARCHAR(36),
    visit_objective     TEXT,
    content_json        TEXT COMMENT 'JSON-serialized full PrevisitReportContent',
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_prc_journey (journey_id),
    INDEX idx_prc_case   (operating_case_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
