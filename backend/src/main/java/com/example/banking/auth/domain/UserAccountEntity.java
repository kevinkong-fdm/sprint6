package com.example.banking.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_account")
public class UserAccountEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Column(name = "email_normalized", nullable = false, unique = true, length = 254)
    private String emailNormalized;

    @Column(name = "password_hash", nullable = false, length = 512)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "lockout_until")
    private Instant lockoutUntil;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static UserAccountEntity newAccount(String email, String normalizedEmail, String passwordHash) {
        Instant now = Instant.now();
        UserAccountEntity entity = new UserAccountEntity();
        entity.id = UUID.randomUUID().toString();
        entity.email = email;
        entity.emailNormalized = normalizedEmail;
        entity.passwordHash = passwordHash;
        entity.status = AccountStatus.ACTIVE;
        entity.failedLoginAttempts = 0;
        entity.createdAt = now;
        entity.updatedAt = now;
        return entity;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getEmailNormalized() {
        return emailNormalized;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Instant getLockoutUntil() {
        return lockoutUntil;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setLockoutUntil(Instant lockoutUntil) {
        this.lockoutUntil = lockoutUntil;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
        this.updatedAt = Instant.now();
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        this.updatedAt = Instant.now();
    }

    public void touchUpdatedAt() {
        this.updatedAt = Instant.now();
    }
}