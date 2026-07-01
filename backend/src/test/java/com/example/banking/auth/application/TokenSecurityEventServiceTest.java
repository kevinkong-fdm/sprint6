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
class TokenSecurityEventServiceTest {

    @Mock
    private AuthenticationEventRepository repository;

    private TokenSecurityEventService service;

    @BeforeEach
    void setUp() {
        service = new TokenSecurityEventService(repository);
    }

    @Test
    void shouldRecordRefreshAndReuseSecurityEvents() {
        when(repository.save(org.mockito.ArgumentMatchers.any(AuthenticationEventEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, AuthenticationEventEntity.class));

        service.refreshed("user-1", "corr-1");
        service.reuseDetected("user-1", "corr-2");

        ArgumentCaptor<AuthenticationEventEntity> captor = ArgumentCaptor.forClass(AuthenticationEventEntity.class);
        verify(repository, org.mockito.Mockito.times(2)).save(captor.capture());

        assertEquals("TOKEN_REFRESH", ReflectionTestUtils.getField(captor.getAllValues().get(0), "eventType"));
        assertEquals("SUCCESS", ReflectionTestUtils.getField(captor.getAllValues().get(0), "outcome"));
        assertEquals("TOKEN_REUSE", ReflectionTestUtils.getField(captor.getAllValues().get(1), "eventType"));
        assertEquals("AUTH-TOKEN-002", ReflectionTestUtils.getField(captor.getAllValues().get(1), "errorCode"));
    }
}
