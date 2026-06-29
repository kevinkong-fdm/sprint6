package com.example.banking.account.api.dto;

import java.util.List;

public record AccountListResponse(
        List<AccountResponse> items,
        int page,
        int size,
        long total
) {
}
