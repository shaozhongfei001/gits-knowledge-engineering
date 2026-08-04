-- V009: Outreach Script and Meeting Script tables (H2 compatible)
CREATE TABLE outreach_script (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    script_id       VARCHAR(64)  NOT NULL UNIQUE,
    customer_id     VARCHAR(64)  NOT NULL,
    rm_id           VARCHAR(64)  NOT NULL,
    operating_case_id VARCHAR(36),
    journey_id      VARCHAR(36),
    channel         VARCHAR(32)  NOT NULL,
    objective       CLOB,
    opening_line    CLOB,
    talking_points  CLOB,
    risk_reminders  CLOB,
    closing_line    CLOB,
    follow_up_action CLOB,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
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
    meeting_objective CLOB,
    previsit_summary  CLOB,
    agenda_items      CLOB,
    kyc_questions     CLOB,
    product_discussions CLOB,
    risk_points       CLOB,
    closing_summary   CLOB,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_meeting_customer ON meeting_script(customer_id);
CREATE INDEX idx_meeting_case ON meeting_script(operating_case_id);
