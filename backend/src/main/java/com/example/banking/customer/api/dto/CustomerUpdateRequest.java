package com.example.banking.customer.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record CustomerUpdateRequest(
        @Size(min = 1, max = 100) String givenName,
        @Size(min = 1, max = 100) String familyName,
        @Size(max = 30) String phoneNumber,
        @Size(min = 1, max = 20) String preferredLanguage,
        @Valid List<@Valid AddressInput> addresses,
        @Valid List<@Valid ContactPreferenceInput> contactPreferences,
        @Email @Size(max = 254) String email,
        LocalDate dateOfBirth,
        String customerId
) {
}
