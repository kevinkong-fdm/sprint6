CREATE TABLE IF NOT EXISTS user_account (
    id CHAR(36) PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    email_normalized VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(512) NOT NULL,
    status VARCHAR(32) NOT NULL,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    lockout_until TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS credential_record (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    password_hash VARCHAR(512) NOT NULL,
    password_updated_at TIMESTAMP NOT NULL,
    policy_version VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_credential_user FOREIGN KEY (user_id) REFERENCES user_account(id)
);

CREATE TABLE IF NOT EXISTS authentication_event (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NULL,
    event_type VARCHAR(64) NOT NULL,
    outcome VARCHAR(32) NOT NULL,
    error_code VARCHAR(64) NULL,
    correlation_id VARCHAR(64) NOT NULL,
    metadata_json TEXT NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_auth_event_user FOREIGN KEY (user_id) REFERENCES user_account(id)
);

CREATE TABLE IF NOT EXISTS login_attempt_counter (
    account_email_normalized VARCHAR(254) PRIMARY KEY,
    failed_count INT NOT NULL DEFAULT 0,
    window_started_at TIMESTAMP NOT NULL,
    lockout_until TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS password_reset_throttle_counter (
    account_email_normalized VARCHAR(254) PRIMARY KEY,
    request_count INT NOT NULL DEFAULT 0,
    window_started_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS password_reset_request (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    token_hash VARCHAR(512) NOT NULL UNIQUE,
    requested_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP NULL,
    request_ip VARCHAR(128) NULL,
    request_user_agent VARCHAR(512) NULL,
    CONSTRAINT fk_reset_user FOREIGN KEY (user_id) REFERENCES user_account(id)
);

CREATE TABLE IF NOT EXISTS refresh_token_session (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    token_hash VARCHAR(512) NOT NULL UNIQUE,
    family_id CHAR(36) NOT NULL,
    previous_token_id CHAR(36) NULL,
    issued_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP NULL,
    revocation_reason VARCHAR(128) NULL,
    created_ip VARCHAR(128) NULL,
    created_user_agent VARCHAR(512) NULL,
    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES user_account(id),
    CONSTRAINT fk_refresh_previous FOREIGN KEY (previous_token_id) REFERENCES refresh_token_session(id)
);
