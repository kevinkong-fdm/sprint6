package com.example.banking.account.api.dto;

import java.util.List;

public record TransactionHistoryResponse(
        String accountId,
        List<TransactionHistoryItemResponse> items,
        int page,
        int size,
        long total
) {
}
