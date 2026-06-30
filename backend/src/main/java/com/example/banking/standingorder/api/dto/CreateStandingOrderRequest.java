package com.example.banking.standingorder.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record CreateStandingOrderRequest(
        @NotBlank String sourceAccountId,
        @NotBlank String destinationAccountId,
        @NotNull @DecimalMin(value = "0.0001") @Digits(integer = 15, fraction = 4) BigDecimal amount,
        @NotBlank String frequency,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        String executionDayOfWeek,
        Integer executionDayOfMonth,
        @Size(min = 5, max = 5) String executionTime,
        Map<String, Object> externalBankDetails
) {
}
