CREATE TABLE payment_sessions (
    id           UUID PRIMARY KEY,
    channel      VARCHAR(10)  NOT NULL,
    merchant_id  VARCHAR(100) NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_sessions_merchant_id ON payment_sessions (merchant_id);
