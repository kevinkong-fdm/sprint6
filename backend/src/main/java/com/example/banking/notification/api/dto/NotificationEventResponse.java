package com.example.banking.notification.api.dto;

import java.time.Instant;

public record NotificationEventResponse(
        String notificationEventId,
        String standingOrderId,
        String standingOrderExecutionId,
        String eventType,
        String title,
        String message,
        String dispatchStatus,
        int dispatchAttemptCount,
        Instant createdAt,
        Instant dispatchedAt
) {
}
