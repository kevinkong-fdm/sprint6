package com.example.banking.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_token_session")
public class RefreshTokenSessionEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "user_id", nullable = false, columnDefinition = "CHAR(36)")
    private String userId;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "family_id", nullable = false, columnDefinition = "CHAR(36)")
    private String familyId;

    @Column(name = "previous_token_id", columnDefinition = "CHAR(36)")
    private String previousTokenId;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revocation_reason")
    private String revocationReason;

    @Column(name = "created_ip")
    private String createdIp;

    @Column(name = "created_user_agent")
    private String createdUserAgent;

    public static RefreshTokenSessionEntity issue(String userId, String tokenHash, String previousTokenId, String familyId) {
        RefreshTokenSessionEntity session = new RefreshTokenSessionEntity();
        session.id = UUID.randomUUID().toString();
        session.userId = userId;
        session.tokenHash = tokenHash;
        session.previousTokenId = previousTokenId;
        session.familyId = familyId == null ? UUID.randomUUID().toString() : familyId;
        session.issuedAt = Instant.now();
        session.expiresAt = session.issuedAt.plusSeconds(2_592_000);
        return session;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public String getFamilyId() {
        return familyId;
    }

    public String getPreviousTokenId() {
        return previousTokenId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void revoke(String reason) {
        this.revokedAt = Instant.now();
        this.revocationReason = reason;
    }
}