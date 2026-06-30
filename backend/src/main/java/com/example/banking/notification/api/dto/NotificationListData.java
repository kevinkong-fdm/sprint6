package com.example.banking.notification.api.dto;

import java.util.List;

public record NotificationListData(
        List<NotificationEventResponse> items,
        int page,
        int size,
        long total
) {
}
