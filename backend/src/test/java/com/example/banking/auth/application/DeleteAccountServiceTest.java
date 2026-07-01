package com.example.banking.auth.application;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.banking.auth.infrastructure.AuthenticationEventRepository;
import com.example.banking.auth.infrastructure.CredentialRecordRepository;
import com.example.banking.auth.infrastructure.LoginAttemptCounterRepository;
import com.example.banking.auth.infrastructure.PasswordResetRequestRepository;
import com.example.banking.auth.infrastructure.PasswordResetThrottleCounterRepository;
import com.example.banking.auth.infrastructure.RefreshTokenSessionRepository;
import com.example.banking.auth.infrastructure.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteAccountServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private RefreshTokenSessionRepository refreshTokenSessionRepository;

    @Mock
    private PasswordResetRequestRepository passwordResetRequestRepository;

    @Mock
    private CredentialRecordRepository credentialRecordRepository;

    @Mock
    private AuthenticationEventRepository authenticationEventRepository;

    @Mock
    private LoginAttemptCounterRepository loginAttemptCounterRepository;

    @Mock
    private PasswordResetThrottleCounterRepository passwordResetThrottleCounterRepository;

    private DeleteAccountService service;

    @BeforeEach
    void setUp() {
        service = new DeleteAccountService(
                userAccountRepository,
                refreshTokenSessionRepository,
                passwordResetRequestRepository,
                credentialRecordRepository,
                authenticationEventRepository,
                loginAttemptCounterRepository,
                passwordResetThrottleCounterRepository);
    }

    @Test
    void shouldNoOpWhenUserIdIsBlank() {
        service.deleteAccountWithAuthData(" ", "alice@example.com");

        verify(userAccountRepository, never()).deleteById(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldDeleteUserAuthDataWithoutEmailCountersWhenEmailMissing() {
        service.deleteAccountWithAuthData("user-1", " ");

        verify(refreshTokenSessionRepository).deleteByUserId("user-1");
        verify(passwordResetRequestRepository).deleteByUserId("user-1");
        verify(credentialRecordRepository).deleteByUserId("user-1");
        verify(authenticationEventRepository).deleteByUserId("user-1");
        verify(userAccountRepository).deleteById("user-1");
        verify(loginAttemptCounterRepository, never()).deleteById(org.mockito.ArgumentMatchers.anyString());
        verify(passwordResetThrottleCounterRepository, never()).deleteById(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldDeleteUserAuthDataAndEmailCounters() {
        service.deleteAccountWithAuthData("user-1", "alice@example.com");

        verify(refreshTokenSessionRepository).deleteByUserId("user-1");
        verify(passwordResetRequestRepository).deleteByUserId("user-1");
        verify(credentialRecordRepository).deleteByUserId("user-1");
        verify(authenticationEventRepository).deleteByUserId("user-1");
        verify(userAccountRepository).deleteById("user-1");
        verify(loginAttemptCounterRepository).deleteById("alice@example.com");
        verify(passwordResetThrottleCounterRepository).deleteById("alice@example.com");
    }
}
