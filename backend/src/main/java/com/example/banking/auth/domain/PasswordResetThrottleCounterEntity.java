package com.example.banking.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "password_reset_throttle_counter")
public class PasswordResetThrottleCounterEntity {

    @Id
    @Column(name = "account_email_normalized", nullable = false, length = 254)
    private String accountEmailNormalized;

    @Column(name = "request_count", nullable = false)
    private int requestCount;

    @Column(name = "window_started_at", nullable = false)
    private Instant windowStartedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static PasswordResetThrottleCounterEntity create(String emailNormalized) {
        PasswordResetThrottleCounterEntity entity = new PasswordResetThrottleCounterEntity();
        entity.accountEmailNormalized = emailNormalized;
        entity.requestCount = 0;
        entity.windowStartedAt = Instant.now();
        entity.updatedAt = entity.windowStartedAt;
        return entity;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public Instant getWindowStartedAt() {
        return windowStartedAt;
    }

    public void increment() {
        this.requestCount += 1;
        this.updatedAt = Instant.now();
    }

    public void resetWindow() {
        this.requestCount = 0;
        this.windowStartedAt = Instant.now();
        this.updatedAt = this.windowStartedAt;
    }
}
