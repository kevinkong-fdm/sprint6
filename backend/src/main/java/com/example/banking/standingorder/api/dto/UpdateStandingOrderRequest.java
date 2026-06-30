package com.example.banking.standingorder.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateStandingOrderRequest(
        String destinationAccountId,
        @DecimalMin(value = "0.0001") @Digits(integer = 15, fraction = 4) BigDecimal amount,
        String frequency,
        LocalDate endDate,
        String executionDayOfWeek,
        Integer executionDayOfMonth,
        @Size(min = 5, max = 5) String executionTime
) {
}
