package com.example.banking.customer.api.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record CustomerResponse(
        String customerId,
        String email,
        String givenName,
        String familyName,
        String phoneNumber,
        LocalDate dateOfBirth,
        String preferredLanguage,
        List<AddressOutput> addresses,
        List<ContactPreferenceOutput> contactPreferences,
        Instant createdAt,
        Instant updatedAt
) {
}
