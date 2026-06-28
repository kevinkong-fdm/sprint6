package com.example.banking.auth.application;

import com.example.banking.auth.infrastructure.AuthenticationEventRepository;
import com.example.banking.auth.infrastructure.CredentialRecordRepository;
import com.example.banking.auth.infrastructure.LoginAttemptCounterRepository;
import com.example.banking.auth.infrastructure.PasswordResetRequestRepository;
import com.example.banking.auth.infrastructure.PasswordResetThrottleCounterRepository;
import com.example.banking.auth.infrastructure.RefreshTokenSessionRepository;
import com.example.banking.auth.infrastructure.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteAccountService {

    private final UserAccountRepository userAccountRepository;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final PasswordResetRequestRepository passwordResetRequestRepository;
    private final CredentialRecordRepository credentialRecordRepository;
    private final AuthenticationEventRepository authenticationEventRepository;
    private final LoginAttemptCounterRepository loginAttemptCounterRepository;
    private final PasswordResetThrottleCounterRepository passwordResetThrottleCounterRepository;

    public DeleteAccountService(
            UserAccountRepository userAccountRepository,
            RefreshTokenSessionRepository refreshTokenSessionRepository,
            PasswordResetRequestRepository passwordResetRequestRepository,
            CredentialRecordRepository credentialRecordRepository,
            AuthenticationEventRepository authenticationEventRepository,
            LoginAttemptCounterRepository loginAttemptCounterRepository,
            PasswordResetThrottleCounterRepository passwordResetThrottleCounterRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
        this.passwordResetRequestRepository = passwordResetRequestRepository;
        this.credentialRecordRepository = credentialRecordRepository;
        this.authenticationEventRepository = authenticationEventRepository;
        this.loginAttemptCounterRepository = loginAttemptCounterRepository;
        this.passwordResetThrottleCounterRepository = passwordResetThrottleCounterRepository;
    }

    @Transactional
    public void deleteAccountWithAuthData(String userId, String emailNormalized) {
        if (userId == null || userId.isBlank()) {
            return;
        }

        refreshTokenSessionRepository.deleteByUserId(userId);
        passwordResetRequestRepository.deleteByUserId(userId);
        credentialRecordRepository.deleteByUserId(userId);
        authenticationEventRepository.deleteByUserId(userId);

        userAccountRepository.deleteById(userId);

        if (emailNormalized != null && !emailNormalized.isBlank()) {
            loginAttemptCounterRepository.deleteById(emailNormalized);
            passwordResetThrottleCounterRepository.deleteById(emailNormalized);
        }
    }
}