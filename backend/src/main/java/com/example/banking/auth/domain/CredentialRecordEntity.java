package com.example.banking.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "credential_record")
public class CredentialRecordEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "user_id", nullable = false, columnDefinition = "CHAR(36)")
    private String userId;

    @Column(name = "password_hash", nullable = false, length = 512)
    private String passwordHash;

    @Column(name = "password_updated_at", nullable = false)
    private Instant passwordUpdatedAt;

    @Column(name = "policy_version", nullable = false, length = 32)
    private String policyVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static CredentialRecordEntity of(String userId, String passwordHash) {
        Instant now = Instant.now();
        CredentialRecordEntity entity = new CredentialRecordEntity();
        entity.id = UUID.randomUUID().toString();
        entity.userId = userId;
        entity.passwordHash = passwordHash;
        entity.passwordUpdatedAt = now;
        entity.policyVersion = "v1";
        entity.createdAt = now;
        entity.updatedAt = now;
        return entity;
    }

    public String getUserId() {
        return userId;
    }
}