package com.example.banking.statement.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record GenerateMonthlyStatementRequest(
        @NotBlank String accountId,
        @NotBlank @Pattern(regexp = "^[0-9]{4}-(0[1-9]|1[0-2])$") String month
) {
}
