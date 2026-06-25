package com.example.banking.auth.application;

import com.example.banking.auth.domain.AuthenticationEventEntity;
import com.example.banking.auth.infrastructure.AuthenticationEventRepository;
import org.springframework.stereotype.Service;

@Service
public class TokenSecurityEventService {

    private final AuthenticationEventRepository repository;

    public TokenSecurityEventService(AuthenticationEventRepository repository) {
        this.repository = repository;
    }

    public void refreshed(String userId, String correlationId) {
        repository.save(AuthenticationEventEntity.of(userId, "TOKEN_REFRESH", "SUCCESS", null, correlationId, "{}"));
    }

    public void reuseDetected(String userId, String correlationId) {
        repository.save(AuthenticationEventEntity.of(userId, "TOKEN_REUSE", "FAILURE", "AUTH-TOKEN-002", correlationId, "{}"));
    }
}
