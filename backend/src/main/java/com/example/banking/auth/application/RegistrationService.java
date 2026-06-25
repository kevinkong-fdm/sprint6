package com.example.banking.auth.application;

import com.example.banking.auth.api.dto.RegisterRequest;
import com.example.banking.auth.api.dto.RegisterResponse;
import com.example.banking.auth.domain.AuthenticationEventEntity;
import com.example.banking.auth.domain.CredentialRecordEntity;
import com.example.banking.auth.domain.UserAccountEntity;
import com.example.banking.auth.infrastructure.AuthenticationEventRepository;
import com.example.banking.auth.infrastructure.CredentialRecordRepository;
import com.example.banking.auth.infrastructure.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RegistrationService {

    private final UserAccountRepository userAccountRepository;
    private final CredentialRecordRepository credentialRecordRepository;
    private final AuthenticationEventRepository eventRepository;
    private final PasswordHashService passwordHashService;

    public RegistrationService(
            UserAccountRepository userAccountRepository,
            CredentialRecordRepository credentialRecordRepository,
            AuthenticationEventRepository eventRepository,
            PasswordHashService passwordHashService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.credentialRecordRepository = credentialRecordRepository;
        this.eventRepository = eventRepository;
        this.passwordHashService = passwordHashService;
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
        eventRepository.save(AuthenticationEventEntity.of(user.getId(), "REGISTER", "SUCCESS", null, effectiveCorrelationId, "{}"));

        return new RegisterResponse(user.getId(), user.getEmail(), user.getCreatedAt());
    }
}