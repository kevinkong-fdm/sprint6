package com.example.banking.standingorder.api.dto;

import java.util.List;

public record StandingOrderExecutionListData(
        List<StandingOrderExecutionResponse> items,
        int page,
        int size,
        long total
) {
}
