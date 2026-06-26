package com.example.banking.customer.api.dto;

import com.example.banking.customer.domain.ContactChannel;

public record ContactPreferenceOutput(
        String preferenceId,
        ContactChannel channel,
        boolean optIn
) {
}
