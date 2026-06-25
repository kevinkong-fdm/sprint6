package com.example.banking.auth.application;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService {

    private static final int MAX_FAILED_ATTEMPTS = 10;

    private final UserAccountRepository userAccountRepository;
    private final LoginAttemptCounterRepository loginAttemptCounterRepository;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final PasswordHashService passwordHashService;
    private final JwtTokenService jwtTokenService;
    private final LoginAuditService loginAuditService;

    public LoginService(
            UserAccountRepository userAccountRepository,
            LoginAttemptCounterRepository loginAttemptCounterRepository,
            RefreshTokenSessionRepository refreshTokenSessionRepository,
            PasswordHashService passwordHashService,
            JwtTokenService jwtTokenService,
            LoginAuditService loginAuditService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.loginAttemptCounterRepository = loginAttemptCounterRepository;
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
        this.passwordHashService = passwordHashService;
        this.jwtTokenService = jwtTokenService;
        this.loginAuditService = loginAuditService;
    }

    @Transactional
    public AuthTokenResponse login(LoginRequest request, String correlationId) {
        String normalizedEmail = EmailNormalizer.normalize(request.email());
        UserAccountEntity user = userAccountRepository.findByEmailNormalized(normalizedEmail)
                .orElseThrow(() -> new DomainException("AUTH-LOGIN-001", "Invalid credentials.", 401));

        if (user.getStatus() == AccountStatus.SUSPENDED) {
            loginAuditService.failure(user.getId(), correlationId, "AUTH-LOGIN-001");
            throw new DomainException("AUTH-LOGIN-001", "Invalid credentials.", 401);
        }

        LoginAttemptCounterEntity counter = loginAttemptCounterRepository.findById(normalizedEmail)
                .orElse(LoginAttemptCounterEntity.create(normalizedEmail));

        if (counter.getLockoutUntil() != null && counter.getLockoutUntil().isAfter(Instant.now())) {
            loginAuditService.lockout(user.getId(), correlationId);
            throw new DomainException("AUTH-LOGIN-002", "Account temporarily locked.", 423);
        }

        if (!passwordHashService.verify(request.password(), user.getPasswordHash())) {
            counter.recordFailure();
            if (counter.getFailedCount() >= MAX_FAILED_ATTEMPTS) {
                counter.lockForMinutes(15);
                user.setStatus(AccountStatus.LOCKED);
                user.setLockoutUntil(counter.getLockoutUntil());
                loginAuditService.lockout(user.getId(), correlationId);
                loginAttemptCounterRepository.save(counter);
                userAccountRepository.save(user);
                throw new DomainException("AUTH-LOGIN-002", "Account temporarily locked.", 423);
            }

            loginAuditService.failure(user.getId(), correlationId, "AUTH-LOGIN-001");
            loginAttemptCounterRepository.save(counter);
            throw new DomainException("AUTH-LOGIN-001", "Invalid credentials.", 401);
        }

        counter.clear();
        user.setStatus(AccountStatus.ACTIVE);
        user.setLockoutUntil(null);
        loginAttemptCounterRepository.save(counter);
        userAccountRepository.save(user);

        JwtTokenService.TokenPair pair = jwtTokenService.issueTokenPair(user.getId(), user.getEmail());
        refreshTokenSessionRepository.save(
                RefreshTokenSessionEntity.issue(
                        user.getId(),
                        jwtTokenService.tokenHash(pair.refreshToken()),
                        null,
                        null
                )
        );

        loginAuditService.success(user.getId(), correlationId);
        return AuthTokenResponse.from(pair);
    }
}
