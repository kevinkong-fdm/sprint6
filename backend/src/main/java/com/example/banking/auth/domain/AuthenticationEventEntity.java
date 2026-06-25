package com.example.banking.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "authentication_event")
public class AuthenticationEventEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "user_id", columnDefinition = "CHAR(36)")
    private String userId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "outcome", nullable = false, length = 32)
    private String outcome;

    @Column(name = "error_code", length = 64)
    private String errorCode;

    @Column(name = "correlation_id", nullable = false, length = 64)
    private String correlationId;

    @Column(name = "metadata_json", nullable = false, columnDefinition = "TEXT")
    private String metadataJson;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public static AuthenticationEventEntity of(String userId, String eventType, String outcome, String errorCode, String correlationId, String metadataJson) {
        AuthenticationEventEntity entity = new AuthenticationEventEntity();
        entity.id = UUID.randomUUID().toString();
        entity.userId = userId;
        entity.eventType = eventType;
        entity.outcome = outcome;
        entity.errorCode = errorCode;
        entity.correlationId = correlationId;
        entity.metadataJson = metadataJson == null ? "{}" : metadataJson;
        entity.occurredAt = Instant.now();
        return entity;
    }
}