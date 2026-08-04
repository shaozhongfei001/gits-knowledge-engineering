-- V008: 交易流水表
CREATE TABLE IF NOT EXISTS transaction (
    id              VARCHAR(36)  NOT NULL,
    transaction_id  VARCHAR(36)  NOT NULL,
    customer_id     VARCHAR(36)  NOT NULL,
    account_id      VARCHAR(36),
    transaction_type VARCHAR(32) NOT NULL,
    amount          DECIMAL(18,2) NOT NULL,
    currency        VARCHAR(3)   NOT NULL DEFAULT 'CNY',
    counterparty    VARCHAR(128),
    counterparty_industry VARCHAR(64),
    description     TEXT,
    transaction_date DATE       NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_transaction PRIMARY KEY (id),
    CONSTRAINT uk_transaction_id UNIQUE (transaction_id),
    CONSTRAINT ck_transaction_type CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT', 'LOAN_DISBURSE', 'LOAN_REPAY', 'TRADE_SETTLEMENT', 'FEE'))
);

CREATE INDEX idx_txn_customer ON transaction (customer_id);
CREATE INDEX idx_txn_date ON transaction (transaction_date);
CREATE INDEX idx_txn_type ON transaction (transaction_type);
