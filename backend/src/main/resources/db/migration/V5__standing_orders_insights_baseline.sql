CREATE TABLE IF NOT EXISTS standing_order (
    standing_order_id CHAR(36) PRIMARY KEY,
    customer_id CHAR(36) NOT NULL,
    source_account_id CHAR(36) NOT NULL,
    destination_account_id CHAR(36) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    frequency VARCHAR(16) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    execution_day_of_week VARCHAR(16) NULL,
    execution_day_of_month INT NULL,
    execution_time VARCHAR(5) NOT NULL,
    status VARCHAR(16) NOT NULL,
    timezone_code VARCHAR(16) NOT NULL,
    next_execution_at TIMESTAMP NULL,
    last_execution_at TIMESTAMP NULL,
    idempotency_key VARCHAR(120) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_standing_order_customer FOREIGN KEY (customer_id) REFERENCES user_account(id) ON DELETE CASCADE,
    CONSTRAINT fk_standing_order_source FOREIGN KEY (source_account_id) REFERENCES bank_account(account_id) ON DELETE CASCADE,
    CONSTRAINT fk_standing_order_destination FOREIGN KEY (destination_account_id) REFERENCES bank_account(account_id) ON DELETE CASCADE
);

CREATE INDEX idx_standing_order_customer_id ON standing_order(customer_id);
CREATE INDEX idx_standing_order_next_execution_at ON standing_order(next_execution_at);
CREATE UNIQUE INDEX uq_standing_order_create_idempotency ON standing_order(customer_id, idempotency_key);

CREATE TABLE IF NOT EXISTS standing_order_execution (
    standing_order_execution_id CHAR(36) PRIMARY KEY,
    standing_order_id CHAR(36) NOT NULL,
    scheduled_for TIMESTAMP NOT NULL,
    triggered_at TIMESTAMP NOT NULL,
    outcome VARCHAR(16) NOT NULL,
    failure_reason_code VARCHAR(32) NULL,
    transfer_reference_id CHAR(36) NULL,
    idempotency_key VARCHAR(120) NULL,
    correlation_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_standing_order_execution_parent FOREIGN KEY (standing_order_id) REFERENCES standing_order(standing_order_id) ON DELETE CASCADE
);

CREATE INDEX idx_standing_order_execution_parent ON standing_order_execution(standing_order_id);
CREATE INDEX idx_standing_order_execution_triggered_at ON standing_order_execution(triggered_at);
CREATE UNIQUE INDEX uq_standing_order_execution_cycle ON standing_order_execution(standing_order_id, scheduled_for);
CREATE UNIQUE INDEX uq_standing_order_execution_idempotency ON standing_order_execution(standing_order_id, idempotency_key);

CREATE TABLE IF NOT EXISTS notification_preference (
    customer_id CHAR(36) PRIMARY KEY,
    standing_order_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    managed_by_system BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_notification_preference_customer FOREIGN KEY (customer_id) REFERENCES user_account(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notification_event (
    notification_event_id CHAR(36) PRIMARY KEY,
    customer_id CHAR(36) NOT NULL,
    standing_order_id CHAR(36) NULL,
    standing_order_execution_id CHAR(36) NULL,
    event_type VARCHAR(32) NOT NULL,
    title VARCHAR(140) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    dispatch_status VARCHAR(16) NOT NULL,
    dispatch_attempt_count INT NOT NULL DEFAULT 0,
    dedupe_key VARCHAR(180) NULL,
    correlation_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    dispatched_at TIMESTAMP NULL,
    CONSTRAINT fk_notification_event_customer FOREIGN KEY (customer_id) REFERENCES user_account(id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_event_order FOREIGN KEY (standing_order_id) REFERENCES standing_order(standing_order_id) ON DELETE SET NULL,
    CONSTRAINT fk_notification_event_execution FOREIGN KEY (standing_order_execution_id) REFERENCES standing_order_execution(standing_order_execution_id) ON DELETE SET NULL
);

CREATE INDEX idx_notification_event_customer_id ON notification_event(customer_id);
CREATE INDEX idx_notification_event_created_at ON notification_event(created_at);
CREATE INDEX idx_notification_event_dispatch_status ON notification_event(dispatch_status);
CREATE UNIQUE INDEX uq_notification_event_dedupe_key ON notification_event(dedupe_key);

CREATE TABLE IF NOT EXISTS monthly_statement (
    monthly_statement_id CHAR(36) PRIMARY KEY,
    account_id CHAR(36) NOT NULL,
    customer_id CHAR(36) NOT NULL,
    statement_month CHAR(7) NOT NULL,
    timezone_code VARCHAR(16) NOT NULL,
    opening_balance DECIMAL(19,4) NOT NULL,
    closing_balance DECIMAL(19,4) NOT NULL,
    total_debits DECIMAL(19,4) NOT NULL,
    total_credits DECIMAL(19,4) NOT NULL,
    line_item_count INT NOT NULL,
    generated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_monthly_statement_account FOREIGN KEY (account_id) REFERENCES bank_account(account_id) ON DELETE CASCADE,
    CONSTRAINT fk_monthly_statement_customer FOREIGN KEY (customer_id) REFERENCES user_account(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uq_monthly_statement_account_month ON monthly_statement(account_id, statement_month);
CREATE INDEX idx_monthly_statement_customer_month ON monthly_statement(customer_id, statement_month);

CREATE TABLE IF NOT EXISTS statement_line_item (
    statement_line_item_id CHAR(36) PRIMARY KEY,
    monthly_statement_id CHAR(36) NOT NULL,
    transaction_id CHAR(36) NOT NULL,
    posted_at TIMESTAMP NOT NULL,
    entry_type VARCHAR(16) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    balance_after DECIMAL(19,4) NOT NULL,
    description VARCHAR(200) NOT NULL,
    CONSTRAINT fk_statement_line_item_statement FOREIGN KEY (monthly_statement_id) REFERENCES monthly_statement(monthly_statement_id) ON DELETE CASCADE
);

CREATE INDEX idx_statement_line_item_statement_id ON statement_line_item(monthly_statement_id);
CREATE INDEX idx_statement_line_item_posted_at ON statement_line_item(posted_at);

CREATE TABLE IF NOT EXISTS spending_insight_snapshot (
    insight_snapshot_id CHAR(36) PRIMARY KEY,
    customer_id CHAR(36) NOT NULL,
    account_id CHAR(36) NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    comparison_mode VARCHAR(32) NOT NULL,
    insufficient_data BOOLEAN NOT NULL,
    insufficiency_reason VARCHAR(64) NOT NULL,
    generated_at TIMESTAMP NOT NULL,
    correlation_id VARCHAR(64) NOT NULL,
    CONSTRAINT fk_spending_insight_customer FOREIGN KEY (customer_id) REFERENCES user_account(id) ON DELETE CASCADE,
    CONSTRAINT fk_spending_insight_account FOREIGN KEY (account_id) REFERENCES bank_account(account_id) ON DELETE SET NULL
);

CREATE INDEX idx_spending_insight_customer_period ON spending_insight_snapshot(customer_id, period_start, period_end);

CREATE TABLE IF NOT EXISTS spending_category_metric (
    spending_category_metric_id CHAR(36) PRIMARY KEY,
    insight_snapshot_id CHAR(36) NOT NULL,
    category_code VARCHAR(64) NOT NULL,
    current_total DECIMAL(19,4) NOT NULL,
    previous_total DECIMAL(19,4) NULL,
    delta_amount DECIMAL(19,4) NULL,
    delta_percent DECIMAL(9,4) NULL,
    CONSTRAINT fk_spending_category_snapshot FOREIGN KEY (insight_snapshot_id) REFERENCES spending_insight_snapshot(insight_snapshot_id) ON DELETE CASCADE
);

CREATE INDEX idx_spending_category_snapshot_id ON spending_category_metric(insight_snapshot_id);
