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
class LoginAuditServiceTest {

    @Mock
    private AuthenticationEventRepository repository;

    private LoginAuditService service;

    @BeforeEach
    void setUp() {
        service = new LoginAuditService(repository);
    }

    @Test
    void shouldRecordSuccessFailureAndLockout() {
        when(repository.save(org.mockito.ArgumentMatchers.any(AuthenticationEventEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, AuthenticationEventEntity.class));

        service.success("user-1", "corr-1");
        service.failure("user-1", "corr-2", "AUTH-LOGIN-001");
        service.lockout("user-1", "corr-3");

        ArgumentCaptor<AuthenticationEventEntity> captor = ArgumentCaptor.forClass(AuthenticationEventEntity.class);
        verify(repository, org.mockito.Mockito.times(3)).save(captor.capture());

        assertEquals("LOGIN", ReflectionTestUtils.getField(captor.getAllValues().get(0), "eventType"));
        assertEquals("LOGIN_FAIL", ReflectionTestUtils.getField(captor.getAllValues().get(1), "eventType"));
        assertEquals("AUTH-LOGIN-001", ReflectionTestUtils.getField(captor.getAllValues().get(1), "errorCode"));
        assertEquals("LOGIN_LOCKOUT", ReflectionTestUtils.getField(captor.getAllValues().get(2), "eventType"));
        assertEquals("AUTH-LOGIN-002", ReflectionTestUtils.getField(captor.getAllValues().get(2), "errorCode"));
    }
}
