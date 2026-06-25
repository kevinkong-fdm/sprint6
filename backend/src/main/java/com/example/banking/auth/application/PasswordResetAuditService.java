package com.example.banking.auth.application;

import com.example.banking.auth.domain.AuthenticationEventEntity;
import com.example.banking.auth.infrastructure.AuthenticationEventRepository;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetAuditService {

    private final AuthenticationEventRepository repository;

    public PasswordResetAuditService(AuthenticationEventRepository repository) {
        this.repository = repository;
    }

    public void requested(String userId, String correlationId) {
        repository.save(AuthenticationEventEntity.of(userId, "RESET_REQUEST", "SUCCESS", null, correlationId, "{}"));
    }

    public void throttled(String userId, String correlationId) {
        repository.save(AuthenticationEventEntity.of(userId, "RESET_REQUEST", "FAILURE", "AUTH-RESET-002", correlationId, "{}"));
    }
}
