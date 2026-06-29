CREATE TABLE IF NOT EXISTS bank_account (
    account_id CHAR(36) PRIMARY KEY,
    customer_id CHAR(36) NOT NULL,
    account_type VARCHAR(16) NOT NULL,
    nickname VARCHAR(80) NULL,
    currency_code CHAR(3) NOT NULL,
    available_balance DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    ledger_balance DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_bank_account_user FOREIGN KEY (customer_id) REFERENCES user_account(id) ON DELETE CASCADE
);

CREATE INDEX idx_bank_account_customer_id ON bank_account(customer_id);

CREATE TABLE IF NOT EXISTS account_movement (
    movement_id CHAR(36) PRIMARY KEY,
    account_id CHAR(36) NOT NULL,
    movement_type VARCHAR(32) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    direction VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL,
    balance_before DECIMAL(19,4) NOT NULL,
    balance_after DECIMAL(19,4) NOT NULL,
    idempotency_key VARCHAR(120) NULL,
    correlation_id VARCHAR(64) NOT NULL,
    reference_id CHAR(36) NULL,
    created_at TIMESTAMP NOT NULL,
    posted_at TIMESTAMP NULL,
    CONSTRAINT fk_account_movement_account FOREIGN KEY (account_id) REFERENCES bank_account(account_id) ON DELETE CASCADE
);

CREATE INDEX idx_account_movement_account_id ON account_movement(account_id);
CREATE INDEX idx_account_movement_created_at ON account_movement(created_at);
CREATE INDEX idx_account_movement_reference_id ON account_movement(reference_id);
CREATE UNIQUE INDEX uq_account_movement_idempotency ON account_movement(account_id, movement_type, idempotency_key);

CREATE TABLE IF NOT EXISTS account_delete_request_audit (
    delete_audit_id CHAR(36) PRIMARY KEY,
    account_id CHAR(36) NOT NULL,
    actor_customer_id CHAR(36) NOT NULL,
    requested_closeout_destination_account_id CHAR(36) NULL,
    eligibility_result VARCHAR(64) NOT NULL,
    error_code VARCHAR(32) NULL,
    correlation_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_account_delete_audit_account_id ON account_delete_request_audit(account_id);
CREATE INDEX idx_account_delete_audit_created_at ON account_delete_request_audit(created_at);
