package com.example.banking.customer.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record CustomerCreateRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 100) String givenName,
        @NotBlank @Size(max = 100) String familyName,
        @Size(max = 30) String phoneNumber,
        @Past LocalDate dateOfBirth,
        @Size(max = 20) String preferredLanguage,
        @Valid List<@Valid AddressInput> addresses,
        @Valid List<@Valid ContactPreferenceInput> contactPreferences
) {
}
