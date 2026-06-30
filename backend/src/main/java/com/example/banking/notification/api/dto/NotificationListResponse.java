package com.example.banking.notification.api.dto;

import java.time.Instant;

public record NotificationListResponse(
        String correlationId,
        Instant timestamp,
        NotificationListData data
) {
}
