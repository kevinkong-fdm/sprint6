package com.example.banking.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.auth.api.dto.AcceptedResponse;
import com.example.banking.auth.api.dto.PasswordResetRequest;
import com.example.banking.auth.domain.PasswordResetRequestEntity;
import com.example.banking.auth.domain.PasswordResetThrottleCounterEntity;
import com.example.banking.auth.domain.UserAccountEntity;
import com.example.banking.auth.infrastructure.PasswordResetRequestRepository;
import com.example.banking.auth.infrastructure.PasswordResetThrottleCounterRepository;
import com.example.banking.auth.infrastructure.UserAccountRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PasswordResetRequestServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordResetThrottleCounterRepository throttleCounterRepository;

    @Mock
    private PasswordResetRequestRepository passwordResetRequestRepository;

    @Mock
    private PasswordResetAuditService passwordResetAuditService;

    @Mock
    private JwtTokenService jwtTokenService;

    private PasswordResetRequestService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetRequestService(
                userAccountRepository,
                throttleCounterRepository,
                passwordResetRequestRepository,
                passwordResetAuditService,
                jwtTokenService);
    }

    @Test
    void shouldReturnNeutralMessageForUnknownEmail() {
        when(userAccountRepository.findByEmailNormalized("unknown@example.com")).thenReturn(Optional.empty());

        AcceptedResponse response = service.requestReset(
                new PasswordResetRequest("unknown@example.com"),
                "corr-1",
                "127.0.0.1",
                "agent");

        assertEquals("If the account exists, reset instructions will be sent.", response.message());
        assertEquals("corr-1", response.correlationId());
        verify(throttleCounterRepository, never()).save(any(PasswordResetThrottleCounterEntity.class));
        verify(passwordResetRequestRepository, never()).save(any(PasswordResetRequestEntity.class));
    }

    @Test
    void shouldThrottleAfterLimit() {
        UserAccountEntity user = UserAccountEntity.newAccount("alice@example.com", "alice@example.com", "hash");
        PasswordResetThrottleCounterEntity counter = PasswordResetThrottleCounterEntity.create("alice@example.com");
        ReflectionTestUtils.setField(counter, "requestCount", 5);

        when(userAccountRepository.findByEmailNormalized("alice@example.com")).thenReturn(Optional.of(user));
        when(throttleCounterRepository.findById("alice@example.com")).thenReturn(Optional.of(counter));

        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.requestReset(
                        new PasswordResetRequest("alice@example.com"),
                        "corr-1",
                        "127.0.0.1",
                        "agent"));

        assertEquals("AUTH-RESET-002", ex.getErrorCode());
        assertEquals(429, ex.getStatus());
        verify(passwordResetAuditService).throttled(eq(user.getId()), eq("corr-1"));
        verify(throttleCounterRepository).save(counter);
    }

    @Test
    void shouldResetWindowAndPersistRequest() {
        UserAccountEntity user = UserAccountEntity.newAccount("alice@example.com", "alice@example.com", "hash");
        PasswordResetThrottleCounterEntity counter = PasswordResetThrottleCounterEntity.create("alice@example.com");
        ReflectionTestUtils.setField(counter, "requestCount", 4);
        ReflectionTestUtils.setField(counter, "windowStartedAt", Instant.now().minusSeconds(3_700));

        when(userAccountRepository.findByEmailNormalized("alice@example.com")).thenReturn(Optional.of(user));
        when(throttleCounterRepository.findById("alice@example.com")).thenReturn(Optional.of(counter));
        when(jwtTokenService.tokenHash(any(String.class))).thenReturn("token-hash");
        when(passwordResetRequestRepository.save(any(PasswordResetRequestEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, PasswordResetRequestEntity.class));

        AcceptedResponse response = service.requestReset(
                new PasswordResetRequest("alice@example.com"),
                "corr-2",
                "127.0.0.1",
                "agent");

        assertEquals("If the account exists, reset instructions will be sent.", response.message());

        ArgumentCaptor<PasswordResetThrottleCounterEntity> counterCaptor =
                ArgumentCaptor.forClass(PasswordResetThrottleCounterEntity.class);
        verify(throttleCounterRepository).save(counterCaptor.capture());
        assertEquals(1, counterCaptor.getValue().getRequestCount());

        verify(passwordResetRequestRepository).save(any(PasswordResetRequestEntity.class));
        verify(passwordResetAuditService).requested(eq(user.getId()), eq("corr-2"));
    }
}
