CREATE TABLE IF NOT EXISTS customer_profile (
    customer_id CHAR(36) PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    email_normalized VARCHAR(254) NOT NULL UNIQUE,
    given_name VARCHAR(100) NOT NULL,
    family_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(30) NULL,
    date_of_birth DATE NULL,
    preferred_language VARCHAR(20) NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(128) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(128) NOT NULL
);

CREATE TABLE IF NOT EXISTS customer_address (
    address_id CHAR(36) PRIMARY KEY,
    customer_id CHAR(36) NOT NULL,
    address_type VARCHAR(16) NOT NULL,
    line1 VARCHAR(200) NOT NULL,
    line2 VARCHAR(200) NULL,
    city VARCHAR(100) NOT NULL,
    region VARCHAR(100) NULL,
    postal_code VARCHAR(20) NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_customer_address_profile FOREIGN KEY (customer_id)
        REFERENCES customer_profile(customer_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS customer_contact_preference (
    preference_id CHAR(36) PRIMARY KEY,
    customer_id CHAR(36) NOT NULL,
    channel VARCHAR(16) NOT NULL,
    opt_in BOOLEAN NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(128) NOT NULL,
    CONSTRAINT fk_customer_preference_profile FOREIGN KEY (customer_id)
        REFERENCES customer_profile(customer_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS customer_lifecycle_event (
    event_id CHAR(36) PRIMARY KEY,
    customer_id CHAR(36) NULL,
    action VARCHAR(16) NOT NULL,
    outcome VARCHAR(16) NOT NULL,
    error_code VARCHAR(64) NULL,
    actor_id VARCHAR(128) NOT NULL,
    correlation_id VARCHAR(64) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    metadata_json TEXT NOT NULL
);

CREATE INDEX idx_customer_profile_email_normalized ON customer_profile(email_normalized);
CREATE INDEX idx_customer_address_customer_id ON customer_address(customer_id);
CREATE INDEX idx_customer_contact_preference_customer_id ON customer_contact_preference(customer_id);
CREATE INDEX idx_customer_lifecycle_event_customer_id ON customer_lifecycle_event(customer_id);
