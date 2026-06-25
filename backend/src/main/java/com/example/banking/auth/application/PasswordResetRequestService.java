package com.example.banking.auth.application;

import com.example.banking.auth.api.dto.AcceptedResponse;
import com.example.banking.auth.api.dto.PasswordResetRequest;
import com.example.banking.auth.domain.PasswordResetRequestEntity;
import com.example.banking.auth.domain.PasswordResetThrottleCounterEntity;
import com.example.banking.auth.domain.UserAccountEntity;
import com.example.banking.auth.infrastructure.PasswordResetRequestRepository;
import com.example.banking.auth.infrastructure.PasswordResetThrottleCounterRepository;
import com.example.banking.auth.infrastructure.UserAccountRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetRequestService {

    private static final String NEUTRAL_MESSAGE = "If the account exists, reset instructions will be sent.";

    private final UserAccountRepository userAccountRepository;
    private final PasswordResetThrottleCounterRepository throttleCounterRepository;
    private final PasswordResetRequestRepository passwordResetRequestRepository;
    private final PasswordResetAuditService passwordResetAuditService;
    private final JwtTokenService jwtTokenService;

    public PasswordResetRequestService(
            UserAccountRepository userAccountRepository,
            PasswordResetThrottleCounterRepository throttleCounterRepository,
            PasswordResetRequestRepository passwordResetRequestRepository,
            PasswordResetAuditService passwordResetAuditService,
            JwtTokenService jwtTokenService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.throttleCounterRepository = throttleCounterRepository;
        this.passwordResetRequestRepository = passwordResetRequestRepository;
        this.passwordResetAuditService = passwordResetAuditService;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AcceptedResponse requestReset(
            PasswordResetRequest request,
            String correlationId,
            String requestIp,
            String requestUserAgent
    ) {
        String normalizedEmail = EmailNormalizer.normalize(request.email());

        Optional<UserAccountEntity> userOpt = userAccountRepository.findByEmailNormalized(normalizedEmail);
        if (userOpt.isEmpty()) {
            return new AcceptedResponse(NEUTRAL_MESSAGE, correlationId);
        }

        UserAccountEntity user = userOpt.get();
        PasswordResetThrottleCounterEntity counter = throttleCounterRepository.findById(normalizedEmail)
                .orElse(PasswordResetThrottleCounterEntity.create(normalizedEmail));

        if (Duration.between(counter.getWindowStartedAt(), Instant.now()).toMinutes() >= 60) {
            counter.resetWindow();
        }

        if (counter.getRequestCount() >= 5) {
            passwordResetAuditService.throttled(user.getId(), correlationId);
            throttleCounterRepository.save(counter);
            throw new DomainException("AUTH-RESET-002", "Password reset request throttled.", 429);
        }

        counter.increment();
        throttleCounterRepository.save(counter);

        String randomToken = UUID.randomUUID().toString() + UUID.randomUUID();
        String tokenHash = jwtTokenService.tokenHash(randomToken);
        passwordResetRequestRepository.save(
                PasswordResetRequestEntity.create(user.getId(), tokenHash, requestIp, requestUserAgent)
        );
        passwordResetAuditService.requested(user.getId(), correlationId);

        return new AcceptedResponse(NEUTRAL_MESSAGE, correlationId);
    }
}
