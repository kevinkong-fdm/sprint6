package com.example.banking.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customer_lifecycle_event")
public class CustomerLifecycleEventEntity {

    @Id
    @Column(name = "event_id", nullable = false, columnDefinition = "CHAR(36)")
    private String eventId;

    @Column(name = "customer_id", columnDefinition = "CHAR(36)")
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 16)
    private LifecycleAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 16)
    private LifecycleOutcome outcome;

    @Column(name = "error_code", length = 64)
    private String errorCode;

    @Column(name = "actor_id", nullable = false, length = 128)
    private String actorId;

    @Column(name = "correlation_id", nullable = false, length = 64)
    private String correlationId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "metadata_json", nullable = false, columnDefinition = "TEXT")
    private String metadataJson;

    public static CustomerLifecycleEventEntity of(
            String customerId,
            LifecycleAction action,
            LifecycleOutcome outcome,
            String errorCode,
            String actorId,
            String correlationId,
            String metadataJson
    ) {
        CustomerLifecycleEventEntity entity = new CustomerLifecycleEventEntity();
        entity.eventId = UUID.randomUUID().toString();
        entity.customerId = normalizeNullable(customerId);
        entity.action = action;
        entity.outcome = outcome;
        entity.errorCode = normalizeNullable(errorCode);
        entity.actorId = normalizeRequired(actorId);
        entity.correlationId = normalizeRequired(correlationId);
        entity.occurredAt = Instant.now();
        entity.metadataJson = normalizeMetadata(metadataJson);
        return entity;
    }

    private static String normalizeRequired(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.trim();
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeMetadata(String value) {
        if (value == null || value.isBlank()) {
            return "{}";
        }
        return value;
    }
}
