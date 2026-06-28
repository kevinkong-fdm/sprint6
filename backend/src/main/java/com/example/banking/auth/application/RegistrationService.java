package com.example.banking.auth.application;

import com.example.banking.auth.api.dto.RegisterRequest;
import com.example.banking.auth.api.dto.RegisterResponse;
import com.example.banking.auth.domain.AuthenticationEventEntity;
import com.example.banking.auth.domain.CredentialRecordEntity;
import com.example.banking.auth.domain.UserAccountEntity;
import com.example.banking.auth.infrastructure.AuthenticationEventRepository;
import com.example.banking.auth.infrastructure.CredentialRecordRepository;
import com.example.banking.auth.infrastructure.UserAccountRepository;
import com.example.banking.customer.api.dto.CustomerCreateRequest;
import com.example.banking.customer.application.CreateCustomerService;
import com.example.banking.customer.application.CustomerDomainException;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private final UserAccountRepository userAccountRepository;
    private final CredentialRecordRepository credentialRecordRepository;
    private final AuthenticationEventRepository eventRepository;
    private final PasswordHashService passwordHashService;
    private final CreateCustomerService createCustomerService;

    public RegistrationService(
            UserAccountRepository userAccountRepository,
            CredentialRecordRepository credentialRecordRepository,
            AuthenticationEventRepository eventRepository,
            PasswordHashService passwordHashService,
            CreateCustomerService createCustomerService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.credentialRecordRepository = credentialRecordRepository;
        this.eventRepository = eventRepository;
        this.passwordHashService = passwordHashService;
        this.createCustomerService = createCustomerService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request, String correlationId) {
        String effectiveCorrelationId = (correlationId == null || correlationId.isBlank())
                ? UUID.randomUUID().toString()
                : correlationId;
        String normalizedEmail = EmailNormalizer.normalize(request.email());
        if (userAccountRepository.existsByEmailNormalized(normalizedEmail)) {
            throw new DomainException("AUTH-REG-002", "Duplicate account identifier.", 409);
        }

        String passwordHash = passwordHashService.hash(request.password());
        UserAccountEntity user = userAccountRepository.save(
                UserAccountEntity.newAccount(request.email().trim(), normalizedEmail, passwordHash)
        );
        credentialRecordRepository.save(CredentialRecordEntity.of(user.getId(), passwordHash));

        try {
            createCustomerService.createWithCustomerId(
                    user.getId(),
                    buildCustomerSeedRequest(user.getEmail()),
                    effectiveCorrelationId,
                    user.getId());
        } catch (CustomerDomainException.DuplicateIdentityException ex) {
            throw new DomainException("AUTH-REG-002", "Duplicate account identifier.", 409);
        }

        eventRepository.save(AuthenticationEventEntity.of(user.getId(), "REGISTER", "SUCCESS", null, effectiveCorrelationId, "{}"));

        return new RegisterResponse(user.getId(), user.getEmail(), user.getCreatedAt());
    }

    private CustomerCreateRequest buildCustomerSeedRequest(String email) {
        return new CustomerCreateRequest(
                email,
                deriveGivenName(email),
                deriveFamilyName(email),
                null,
                null,
                null,
                null,
                null
        );
    }

    private String deriveGivenName(String email) {
        String[] tokens = extractNameTokens(email);
        if (tokens.length == 0) {
            return "New";
        }
        String givenName = toDisplayNameToken(tokens[0]);
        return givenName.isBlank() ? "New" : truncate(givenName, 100);
    }

    private String deriveFamilyName(String email) {
        String[] tokens = extractNameTokens(email);
        if (tokens.length < 2) {
            return "Customer";
        }
        String familyName = toDisplayNameToken(tokens[1]);
        return familyName.isBlank() ? "Customer" : truncate(familyName, 100);
    }

    private String[] extractNameTokens(String email) {
        if (email == null || email.isBlank()) {
            return new String[0];
        }

        String localPart = email.trim();
        int atIndex = localPart.indexOf('@');
        if (atIndex >= 0) {
            localPart = localPart.substring(0, atIndex);
        }

        return Arrays.stream(localPart.split("[._+\\-]+"))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .toArray(String[]::new);
    }

    private String toDisplayNameToken(String token) {
        String cleaned = token.replaceAll("[^A-Za-z0-9]", "");
        if (cleaned.isBlank()) {
            return "";
        }

        String normalized = cleaned.toLowerCase(Locale.ROOT);
        if (normalized.length() == 1) {
            return normalized.toUpperCase(Locale.ROOT);
        }

        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}