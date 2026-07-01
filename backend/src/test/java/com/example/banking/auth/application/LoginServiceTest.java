package com.example.banking.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.auth.api.dto.AuthTokenResponse;
import com.example.banking.auth.api.dto.LoginRequest;
import com.example.banking.auth.domain.AccountStatus;
import com.example.banking.auth.domain.LoginAttemptCounterEntity;
import com.example.banking.auth.domain.RefreshTokenSessionEntity;
import com.example.banking.auth.domain.UserAccountEntity;
import com.example.banking.auth.infrastructure.LoginAttemptCounterRepository;
import com.example.banking.auth.infrastructure.RefreshTokenSessionRepository;
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
class LoginServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private LoginAttemptCounterRepository loginAttemptCounterRepository;

    @Mock
    private RefreshTokenSessionRepository refreshTokenSessionRepository;

    @Mock
    private PasswordHashService passwordHashService;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private LoginAuditService loginAuditService;

    private LoginService service;

    @BeforeEach
    void setUp() {
        service = new LoginService(
                userAccountRepository,
                loginAttemptCounterRepository,
                refreshTokenSessionRepository,
                passwordHashService,
                jwtTokenService,
                loginAuditService);
    }

    @Test
    void shouldRejectUnknownEmail() {
        when(userAccountRepository.findByEmailNormalized("alice@example.com")).thenReturn(Optional.empty());

        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.login(new LoginRequest("alice@example.com", "password"), "corr-1"));

        assertEquals("AUTH-LOGIN-001", ex.getErrorCode());
        assertEquals(401, ex.getStatus());
    }

    @Test
    void shouldRejectSuspendedUser() {
        UserAccountEntity user = UserAccountEntity.newAccount("alice@example.com", "alice@example.com", "hash");
        user.setStatus(AccountStatus.SUSPENDED);

        when(userAccountRepository.findByEmailNormalized("alice@example.com")).thenReturn(Optional.of(user));

        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.login(new LoginRequest("alice@example.com", "password"), "corr-1"));

        assertEquals("AUTH-LOGIN-001", ex.getErrorCode());
        verify(loginAuditService).failure(user.getId(), "corr-1", "AUTH-LOGIN-001");
    }

    @Test
    void shouldRejectWhenCounterIsLocked() {
        UserAccountEntity user = UserAccountEntity.newAccount("alice@example.com", "alice@example.com", "hash");
        LoginAttemptCounterEntity counter = LoginAttemptCounterEntity.create("alice@example.com");
        ReflectionTestUtils.setField(counter, "lockoutUntil", Instant.now().plusSeconds(120));

        when(userAccountRepository.findByEmailNormalized("alice@example.com")).thenReturn(Optional.of(user));
        when(loginAttemptCounterRepository.findById("alice@example.com")).thenReturn(Optional.of(counter));

        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.login(new LoginRequest("alice@example.com", "password"), "corr-1"));

        assertEquals("AUTH-LOGIN-002", ex.getErrorCode());
        verify(loginAuditService).lockout(user.getId(), "corr-1");
    }

    @Test
    void shouldIncrementCounterOnInvalidPassword() {
        UserAccountEntity user = UserAccountEntity.newAccount("alice@example.com", "alice@example.com", "hash");
        LoginAttemptCounterEntity counter = LoginAttemptCounterEntity.create("alice@example.com");

        when(userAccountRepository.findByEmailNormalized("alice@example.com")).thenReturn(Optional.of(user));
        when(loginAttemptCounterRepository.findById("alice@example.com")).thenReturn(Optional.of(counter));
        when(passwordHashService.verify("bad-password", "hash")).thenReturn(false);

        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.login(new LoginRequest("alice@example.com", "bad-password"), "corr-1"));

        assertEquals("AUTH-LOGIN-001", ex.getErrorCode());

        ArgumentCaptor<LoginAttemptCounterEntity> counterCaptor = ArgumentCaptor.forClass(LoginAttemptCounterEntity.class);
        verify(loginAttemptCounterRepository).save(counterCaptor.capture());
        assertEquals(1, counterCaptor.getValue().getFailedCount());
        verify(userAccountRepository, never()).save(any(UserAccountEntity.class));
    }

    @Test
    void shouldLockUserAfterThresholdFailures() {
        UserAccountEntity user = UserAccountEntity.newAccount("alice@example.com", "alice@example.com", "hash");
        LoginAttemptCounterEntity counter = LoginAttemptCounterEntity.create("alice@example.com");
        for (int i = 0; i < 9; i++) {
            counter.recordFailure();
        }

        when(userAccountRepository.findByEmailNormalized("alice@example.com")).thenReturn(Optional.of(user));
        when(loginAttemptCounterRepository.findById("alice@example.com")).thenReturn(Optional.of(counter));
        when(passwordHashService.verify("bad-password", "hash")).thenReturn(false);

        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.login(new LoginRequest("alice@example.com", "bad-password"), "corr-1"));

        assertEquals("AUTH-LOGIN-002", ex.getErrorCode());
        assertEquals(AccountStatus.LOCKED, user.getStatus());
        assertNotNull(user.getLockoutUntil());
        verify(loginAttemptCounterRepository).save(counter);
        verify(userAccountRepository).save(user);
        verify(loginAuditService).lockout(user.getId(), "corr-1");
    }

    @Test
    void shouldIssueTokensAndClearCounterOnSuccessfulLogin() {
        UserAccountEntity user = UserAccountEntity.newAccount("alice@example.com", "alice@example.com", "hash");
        user.setStatus(AccountStatus.LOCKED);
        user.setLockoutUntil(Instant.now().plusSeconds(60));

        LoginAttemptCounterEntity counter = LoginAttemptCounterEntity.create("alice@example.com");
        counter.recordFailure();
        counter.recordFailure();

        JwtTokenService.TokenPair tokenPair = new JwtTokenService.TokenPair(
                "Bearer",
                "access-token",
                "refresh-token",
                900,
                1200);

        when(userAccountRepository.findByEmailNormalized("alice@example.com")).thenReturn(Optional.of(user));
        when(loginAttemptCounterRepository.findById("alice@example.com")).thenReturn(Optional.of(counter));
        when(passwordHashService.verify("good-password", "hash")).thenReturn(true);
        when(jwtTokenService.issueTokenPair(user.getId(), user.getEmail())).thenReturn(tokenPair);
        when(jwtTokenService.tokenHash("refresh-token")).thenReturn("refresh-hash");
        when(refreshTokenSessionRepository.save(any(RefreshTokenSessionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, RefreshTokenSessionEntity.class));

        AuthTokenResponse response = service.login(new LoginRequest("alice@example.com", "good-password"), "corr-1");

        assertEquals(user.getId(), response.userId());
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals(0, counter.getFailedCount());
        assertEquals(AccountStatus.ACTIVE, user.getStatus());
        assertNull(user.getLockoutUntil());

        verify(loginAttemptCounterRepository).save(counter);
        verify(userAccountRepository).save(user);
        verify(loginAuditService).success(eq(user.getId()), eq("corr-1"));
        verify(refreshTokenSessionRepository).save(any(RefreshTokenSessionEntity.class));
    }
}
