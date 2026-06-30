package com.example.banking.notification.api.dto;

import jakarta.validation.constraints.NotNull;

public record NotificationPreferenceUpdateRequest(
        @NotNull Boolean standingOrderNotificationsEnabled
) {
}
