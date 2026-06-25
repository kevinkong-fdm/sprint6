package com.example.banking.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "login_attempt_counter")
public class LoginAttemptCounterEntity {

    @Id
    @Column(name = "account_email_normalized", nullable = false, length = 254)
    private String accountEmailNormalized;

    @Column(name = "failed_count", nullable = false)
    private int failedCount;

    @Column(name = "window_started_at", nullable = false)
    private Instant windowStartedAt;

    @Column(name = "lockout_until")
    private Instant lockoutUntil;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static LoginAttemptCounterEntity create(String emailNormalized) {
        LoginAttemptCounterEntity entity = new LoginAttemptCounterEntity();
        entity.accountEmailNormalized = emailNormalized;
        entity.failedCount = 0;
        entity.windowStartedAt = Instant.now();
        entity.updatedAt = entity.windowStartedAt;
        return entity;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public Instant getLockoutUntil() {
        return lockoutUntil;
    }

    public Instant getWindowStartedAt() {
        return windowStartedAt;
    }

    public void recordFailure() {
        this.failedCount += 1;
        this.updatedAt = Instant.now();
    }

    public void lockForMinutes(long minutes) {
        this.lockoutUntil = Instant.now().plusSeconds(minutes * 60);
        this.updatedAt = Instant.now();
    }

    public void clear() {
        this.failedCount = 0;
        this.lockoutUntil = null;
        this.windowStartedAt = Instant.now();
        this.updatedAt = this.windowStartedAt;
    }
}
