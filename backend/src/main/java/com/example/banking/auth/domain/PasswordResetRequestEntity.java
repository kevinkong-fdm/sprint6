package com.example.banking.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_request")
public class PasswordResetRequestEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "user_id", nullable = false, columnDefinition = "CHAR(36)")
    private String userId;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "request_ip")
    private String requestIp;

    @Column(name = "request_user_agent")
    private String requestUserAgent;

    public static PasswordResetRequestEntity create(String userId, String tokenHash, String requestIp, String requestUserAgent) {
        PasswordResetRequestEntity entity = new PasswordResetRequestEntity();
        entity.id = UUID.randomUUID().toString();
        entity.userId = userId;
        entity.tokenHash = tokenHash;
        entity.requestedAt = Instant.now();
        entity.expiresAt = entity.requestedAt.plusSeconds(1800);
        entity.requestIp = requestIp;
        entity.requestUserAgent = requestUserAgent;
        return entity;
    }
}