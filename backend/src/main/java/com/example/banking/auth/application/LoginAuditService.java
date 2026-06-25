package com.example.banking.auth.application;

import com.example.banking.auth.domain.AuthenticationEventEntity;
import com.example.banking.auth.infrastructure.AuthenticationEventRepository;
import org.springframework.stereotype.Service;

@Service
public class LoginAuditService {

    private final AuthenticationEventRepository repository;

    public LoginAuditService(AuthenticationEventRepository repository) {
        this.repository = repository;
    }

    public void success(String userId, String correlationId) {
        repository.save(AuthenticationEventEntity.of(userId, "LOGIN", "SUCCESS", null, correlationId, "{}"));
    }

    public void failure(String userId, String correlationId, String errorCode) {
        repository.save(AuthenticationEventEntity.of(userId, "LOGIN_FAIL", "FAILURE", errorCode, correlationId, "{}"));
    }

    public void lockout(String userId, String correlationId) {
        repository.save(AuthenticationEventEntity.of(userId, "LOGIN_LOCKOUT", "FAILURE", "AUTH-LOGIN-002", correlationId, "{}"));
    }
}
