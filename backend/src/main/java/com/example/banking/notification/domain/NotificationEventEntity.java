package com.example.banking.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_event")
public class NotificationEventEntity {

    @Id
    @Column(name = "notification_event_id", nullable = false, columnDefinition = "CHAR(36)")
    private String notificationEventId;

    @Column(name = "customer_id", nullable = false, columnDefinition = "CHAR(36)")
    private String customerId;

    @Column(name = "standing_order_id", columnDefinition = "CHAR(36)")
    private String standingOrderId;

    @Column(name = "standing_order_execution_id", columnDefinition = "CHAR(36)")
    private String standingOrderExecutionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    private NotificationEventType eventType;

    @Column(name = "title", nullable = false, length = 140)
    private String title;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "dispatch_status", nullable = false, length = 16)
    private NotificationDispatchStatus dispatchStatus;

    @Column(name = "dispatch_attempt_count", nullable = false)
    private int dispatchAttemptCount;

    @Column(name = "dedupe_key", length = 180)
    private String dedupeKey;

    @Column(name = "correlation_id", nullable = false, length = 64)
    private String correlationId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "dispatched_at")
    private Instant dispatchedAt;

    public static NotificationEventEntity pending(
            String customerId,
            String standingOrderId,
            String standingOrderExecutionId,
            NotificationEventType eventType,
            String title,
            String message,
            String dedupeKey,
            String correlationId
    ) {
        NotificationEventEntity entity = new NotificationEventEntity();
        entity.notificationEventId = UUID.randomUUID().toString();
        entity.customerId = normalizeRequired(customerId);
        entity.standingOrderId = normalizeNullable(standingOrderId);
        entity.standingOrderExecutionId = normalizeNullable(standingOrderExecutionId);
        entity.eventType = eventType;
        entity.title = normalizeRequired(title);
        entity.message = normalizeRequired(message);
        entity.dispatchStatus = NotificationDispatchStatus.PENDING;
        entity.dispatchAttemptCount = 0;
        entity.dedupeKey = normalizeNullable(dedupeKey);
        entity.correlationId = normalizeRequired(correlationId);
        entity.createdAt = Instant.now();
        entity.dispatchedAt = null;
        return entity;
    }

    public void markSent() {
        this.dispatchAttemptCount += 1;
        this.dispatchStatus = NotificationDispatchStatus.SENT;
        this.dispatchedAt = Instant.now();
    }

    public void markFailed() {
        this.dispatchAttemptCount += 1;
        this.dispatchStatus = NotificationDispatchStatus.FAILED;
    }

    public String getNotificationEventId() {
        return notificationEventId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getStandingOrderId() {
        return standingOrderId;
    }

    public String getStandingOrderExecutionId() {
        return standingOrderExecutionId;
    }

    public NotificationEventType getEventType() {
        return eventType;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public NotificationDispatchStatus getDispatchStatus() {
        return dispatchStatus;
    }

    public int getDispatchAttemptCount() {
        return dispatchAttemptCount;
    }

    public String getDedupeKey() {
        return dedupeKey;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getDispatchedAt() {
        return dispatchedAt;
    }

    private static String normalizeRequired(String value) {
        if (value == null) {
            return "";
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
}
