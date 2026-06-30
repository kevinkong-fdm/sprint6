package com.example.banking.standingorder.api.dto;

import java.util.List;

public record StandingOrderListData(
        List<StandingOrderResponse> items,
        int page,
        int size,
        long total
) {
}
