-- V009: Outreach Script and Meeting Script tables
CREATE TABLE outreach_script (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    script_id       VARCHAR(64)  NOT NULL UNIQUE,
    customer_id     VARCHAR(64)  NOT NULL,
    rm_id           VARCHAR(64)  NOT NULL,
    operating_case_id VARCHAR(36),
    journey_id      VARCHAR(36),
    channel         VARCHAR(32)  NOT NULL,
    objective       TEXT,
    opening_line    TEXT,
    talking_points  JSON,
    risk_reminders  JSON,
    closing_line    TEXT,
    follow_up_action TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_outreach_customer ON outreach_script(customer_id);
CREATE INDEX idx_outreach_case ON outreach_script(operating_case_id);

CREATE TABLE meeting_script (
    id                VARCHAR(36)  NOT NULL PRIMARY KEY,
    script_id         VARCHAR(64)  NOT NULL UNIQUE,
    customer_id       VARCHAR(64)  NOT NULL,
    rm_id             VARCHAR(64)  NOT NULL,
    operating_case_id VARCHAR(36),
    journey_id        VARCHAR(36),
    meeting_objective TEXT,
    previsit_summary  TEXT,
    agenda_items      JSON,
    kyc_questions     JSON,
    product_discussions JSON,
    risk_points       JSON,
    closing_summary   TEXT,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_meeting_customer ON meeting_script(customer_id);
CREATE INDEX idx_meeting_case ON meeting_script(operating_case_id);
