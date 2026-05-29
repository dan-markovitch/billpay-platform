CREATE TABLE invoices (
    id                  UUID PRIMARY KEY,
    external_reference  VARCHAR(100) NOT NULL,
    amount              NUMERIC(19, 4) NOT NULL,
    currency            VARCHAR(3)   NOT NULL,
    status              VARCHAR(20)  NOT NULL,
    received_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT invoices_amount_positive CHECK (amount > 0)
);

CREATE UNIQUE INDEX idx_invoices_external_reference ON invoices (external_reference);
