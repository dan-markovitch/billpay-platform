CREATE TABLE transactions (
    id               UUID PRIMARY KEY,
    description      VARCHAR(50)  NOT NULL,
    amount           NUMERIC(19, 4) NOT NULL,
    currency         VARCHAR(3)   NOT NULL,
    transaction_date DATE         NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT transactions_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_transactions_transaction_date ON transactions (transaction_date);
