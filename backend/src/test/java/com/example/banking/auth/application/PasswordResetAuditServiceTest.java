package com.example.banking.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.auth.domain.AuthenticationEventEntity;
import com.example.banking.auth.infrastructure.AuthenticationEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PasswordResetAuditServiceTest {

    @Mock
    private AuthenticationEventRepository repository;

    private PasswordResetAuditService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetAuditService(repository);
    }

    @Test
    void shouldRecordRequestedAndThrottledEvents() {
        when(repository.save(org.mockito.ArgumentMatchers.any(AuthenticationEventEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, AuthenticationEventEntity.class));

        service.requested("user-1", "corr-1");
        service.throttled("user-1", "corr-2");

        ArgumentCaptor<AuthenticationEventEntity> captor = ArgumentCaptor.forClass(AuthenticationEventEntity.class);
        verify(repository, org.mockito.Mockito.times(2)).save(captor.capture());

        assertEquals("RESET_REQUEST", ReflectionTestUtils.getField(captor.getAllValues().get(0), "eventType"));
        assertEquals("SUCCESS", ReflectionTestUtils.getField(captor.getAllValues().get(0), "outcome"));
        assertEquals("FAILURE", ReflectionTestUtils.getField(captor.getAllValues().get(1), "outcome"));
        assertEquals("AUTH-RESET-002", ReflectionTestUtils.getField(captor.getAllValues().get(1), "errorCode"));
    }
}
