package com.example.banking.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.auth.api.dto.AuthTokenResponse;
import com.example.banking.auth.api.dto.TokenRefreshRequest;
import com.example.banking.auth.domain.AccountStatus;
import com.example.banking.auth.domain.RefreshTokenSessionEntity;
import com.example.banking.auth.domain.UserAccountEntity;
import com.example.banking.auth.infrastructure.RefreshTokenSessionRepository;
import com.example.banking.auth.infrastructure.UserAccountRepository;
import io.jsonwebtoken.Claims;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenSessionRepository refreshTokenSessionRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private TokenSecurityEventService tokenSecurityEventService;

    @Mock
    private Claims claims;

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(
                refreshTokenSessionRepository,
                userAccountRepository,
                jwtTokenService,
                tokenSecurityEventService);
    }

    @Test
    void shouldRejectMissingSession() {
        when(jwtTokenService.tokenHash("refresh-token")).thenReturn("missing-hash");
        when(refreshTokenSessionRepository.findByTokenHash("missing-hash")).thenReturn(Optional.empty());

        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.refresh(new TokenRefreshRequest("refresh-token"), "corr-1"));

        assertEquals("AUTH-TOKEN-001", ex.getErrorCode());
        assertEquals(401, ex.getStatus());
    }

    @Test
    void shouldRejectRevokedSessionAndEmitReuseEvent() {
        RefreshTokenSessionEntity session = RefreshTokenSessionEntity.issue("user-1", "hash-1", null, null);
        session.revoke("manual");

        when(jwtTokenService.tokenHash("refresh-token")).thenReturn("hash-1");
        when(refreshTokenSessionRepository.findByTokenHash("hash-1")).thenReturn(Optional.of(session));

        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.refresh(new TokenRefreshRequest("refresh-token"), "corr-1"));

        assertEquals("AUTH-TOKEN-002", ex.getErrorCode());
        verify(tokenSecurityEventService).reuseDetected("user-1", "corr-1");
    }

    @Test
    void shouldRejectExpiredSession() {
        RefreshTokenSessionEntity session = RefreshTokenSessionEntity.issue("user-1", "hash-1", null, null);
        ReflectionTestUtils.setField(session, "expiresAt", Instant.now().minusSeconds(10));

        when(jwtTokenService.tokenHash("refresh-token")).thenReturn("hash-1");
        when(refreshTokenSessionRepository.findByTokenHash("hash-1")).thenReturn(Optional.of(session));

        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.refresh(new TokenRefreshRequest("refresh-token"), "corr-1"));

        assertEquals("AUTH-TOKEN-001", ex.getErrorCode());
    }

    @Test
    void shouldRejectWhenUserIsMissing() {
        RefreshTokenSessionEntity session = RefreshTokenSessionEntity.issue("user-1", "hash-1", null, null);

        when(jwtTokenService.tokenHash("refresh-token")).thenReturn("hash-1");
        when(refreshTokenSessionRepository.findByTokenHash("hash-1")).thenReturn(Optional.of(session));
        when(userAccountRepository.findById("user-1")).thenReturn(Optional.empty());

        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.refresh(new TokenRefreshRequest("refresh-token"), "corr-1"));

        assertEquals("AUTH-TOKEN-001", ex.getErrorCode());
    }

    @Test
    void shouldRejectSuspendedUser() {
        RefreshTokenSessionEntity session = RefreshTokenSessionEntity.issue("user-1", "hash-1", null, null);
        UserAccountEntity user = UserAccountEntity.newAccount("a@example.com", "a@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", "user-1");
        user.setStatus(AccountStatus.SUSPENDED);

        when(jwtTokenService.tokenHash("refresh-token")).thenReturn("hash-1");
        when(refreshTokenSessionRepository.findByTokenHash("hash-1")).thenReturn(Optional.of(session));
        when(userAccountRepository.findById("user-1")).thenReturn(Optional.of(user));

        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.refresh(new TokenRefreshRequest("refresh-token"), "corr-1"));

        assertEquals("AUTH-SESSION-001", ex.getErrorCode());
    }

    @Test
    void shouldRotateSessionAndReturnNewTokenPair() {
        RefreshTokenSessionEntity session = RefreshTokenSessionEntity.issue("user-1", "old-hash", null, null);
        UserAccountEntity user = UserAccountEntity.newAccount("a@example.com", "a@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", "user-1");

        JwtTokenService.TokenPair pair = new JwtTokenService.TokenPair(
                "Bearer",
                "access-2",
                "refresh-2",
                900,
                1800);

        when(jwtTokenService.tokenHash("refresh-token")).thenReturn("old-hash");
        when(refreshTokenSessionRepository.findByTokenHash("old-hash")).thenReturn(Optional.of(session));
        when(userAccountRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(jwtTokenService.parse("refresh-token")).thenReturn(claims);
        when(jwtTokenService.issueTokenPair("user-1", "a@example.com")).thenReturn(pair);
        when(jwtTokenService.tokenHash("refresh-2")).thenReturn("new-hash");
        when(refreshTokenSessionRepository.save(any(RefreshTokenSessionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, RefreshTokenSessionEntity.class));

        AuthTokenResponse response = service.refresh(new TokenRefreshRequest("refresh-token"), "corr-1");

        assertEquals("access-2", response.accessToken());
        assertEquals("refresh-2", response.refreshToken());
        assertNotNull(session.getRevokedAt());

        ArgumentCaptor<RefreshTokenSessionEntity> sessionCaptor = ArgumentCaptor.forClass(RefreshTokenSessionEntity.class);
        verify(refreshTokenSessionRepository, times(2)).save(sessionCaptor.capture());

        List<RefreshTokenSessionEntity> saved = sessionCaptor.getAllValues();
        RefreshTokenSessionEntity rotated = saved.get(1);
        assertEquals(session.getId(), rotated.getPreviousTokenId());
        assertEquals(session.getFamilyId(), rotated.getFamilyId());

        verify(tokenSecurityEventService).refreshed(eq("user-1"), eq("corr-1"));
    }
}
