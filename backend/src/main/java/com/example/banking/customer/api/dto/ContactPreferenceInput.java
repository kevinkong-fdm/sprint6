package com.example.banking.customer.api.dto;

import com.example.banking.customer.domain.ContactChannel;
import jakarta.validation.constraints.NotNull;

public record ContactPreferenceInput(
        @NotNull ContactChannel channel,
        @NotNull Boolean optIn
) {
}
