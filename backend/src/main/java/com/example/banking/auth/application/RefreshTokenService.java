package com.example.banking.auth.application;

import com.example.banking.auth.api.dto.AuthTokenResponse;
import com.example.banking.auth.api.dto.TokenRefreshRequest;
import com.example.banking.auth.domain.AccountStatus;
import com.example.banking.auth.domain.RefreshTokenSessionEntity;
import com.example.banking.auth.domain.UserAccountEntity;
import com.example.banking.auth.infrastructure.RefreshTokenSessionRepository;
import com.example.banking.auth.infrastructure.UserAccountRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final UserAccountRepository userAccountRepository;
    private final JwtTokenService jwtTokenService;
    private final TokenSecurityEventService tokenSecurityEventService;

    public RefreshTokenService(
            RefreshTokenSessionRepository refreshTokenSessionRepository,
            UserAccountRepository userAccountRepository,
            JwtTokenService jwtTokenService,
            TokenSecurityEventService tokenSecurityEventService
    ) {
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
        this.userAccountRepository = userAccountRepository;
        this.jwtTokenService = jwtTokenService;
        this.tokenSecurityEventService = tokenSecurityEventService;
    }

    @Transactional
    public AuthTokenResponse refresh(TokenRefreshRequest request, String correlationId) {
        String tokenHash = jwtTokenService.tokenHash(request.refreshToken());
        RefreshTokenSessionEntity session = refreshTokenSessionRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new DomainException("AUTH-TOKEN-001", "Invalid or expired refresh token.", 401));

        if (session.getRevokedAt() != null) {
            tokenSecurityEventService.reuseDetected(session.getUserId(), correlationId);
            throw new DomainException("AUTH-TOKEN-002", "Refresh token reuse detected.", 401);
        }

        if (session.getExpiresAt().isBefore(Instant.now())) {
            throw new DomainException("AUTH-TOKEN-001", "Invalid or expired refresh token.", 401);
        }

        UserAccountEntity user = userAccountRepository.findById(session.getUserId())
                .orElseThrow(() -> new DomainException("AUTH-TOKEN-001", "Invalid or expired refresh token.", 401));

        if (user.getStatus() == AccountStatus.SUSPENDED) {
            throw new DomainException("AUTH-SESSION-001", "Session not active or already terminated.", 401);
        }

        // Validate signature and expiry semantics.
        jwtTokenService.parse(request.refreshToken());

        JwtTokenService.TokenPair tokenPair = jwtTokenService.issueTokenPair(user.getId(), user.getEmail());
        session.revoke("rotated");
        refreshTokenSessionRepository.save(session);

        refreshTokenSessionRepository.save(
                RefreshTokenSessionEntity.issue(
                        user.getId(),
                        jwtTokenService.tokenHash(tokenPair.refreshToken()),
                        session.getId(),
                        session.getFamilyId()
                )
        );

        tokenSecurityEventService.refreshed(user.getId(), correlationId);
        return AuthTokenResponse.from(tokenPair, user.getId());
    }
}
