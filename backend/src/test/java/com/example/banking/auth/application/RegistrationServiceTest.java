package com.example.banking.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.auth.api.dto.RegisterRequest;
import com.example.banking.auth.api.dto.RegisterResponse;
import com.example.banking.auth.domain.AuthenticationEventEntity;
import com.example.banking.auth.domain.CredentialRecordEntity;
import com.example.banking.auth.domain.UserAccountEntity;
import com.example.banking.auth.infrastructure.AuthenticationEventRepository;
import com.example.banking.auth.infrastructure.CredentialRecordRepository;
import com.example.banking.auth.infrastructure.UserAccountRepository;
import com.example.banking.customer.api.dto.CustomerCreateRequest;
import com.example.banking.customer.application.CreateCustomerService;
import com.example.banking.customer.application.CustomerDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private CredentialRecordRepository credentialRecordRepository;

    @Mock
    private AuthenticationEventRepository authenticationEventRepository;

    @Mock
    private PasswordHashService passwordHashService;

    @Mock
    private CreateCustomerService createCustomerService;

    private RegistrationService service;

    @BeforeEach
    void setUp() {
        service = new RegistrationService(
                userAccountRepository,
                credentialRecordRepository,
                authenticationEventRepository,
                passwordHashService,
                createCustomerService);
    }

    @Test
    void shouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("alice@example.com", "Password123456");
        when(userAccountRepository.existsByEmailNormalized("alice@example.com")).thenReturn(true);

        DomainException ex = assertThrows(DomainException.class, () -> service.register(request, "corr-1"));

        assertEquals("AUTH-REG-002", ex.getErrorCode());
        assertEquals(409, ex.getStatus());
        verify(userAccountRepository, never()).save(any(UserAccountEntity.class));
    }

    @Test
    void shouldCreateUserCredentialCustomerAndEvent() {
        RegisterRequest request = new RegisterRequest(" john.doe+test@example.com ", "Password123456");

        when(userAccountRepository.existsByEmailNormalized("john.doe+test@example.com")).thenReturn(false);
        when(passwordHashService.hash("Password123456")).thenReturn("hash-1");
        when(userAccountRepository.save(any(UserAccountEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, UserAccountEntity.class));
        when(credentialRecordRepository.save(any(CredentialRecordEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, CredentialRecordEntity.class));
        when(authenticationEventRepository.save(any(AuthenticationEventEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, AuthenticationEventEntity.class));

        RegisterResponse response = service.register(request, "corr-1");

        assertNotNull(response.userId());
        assertEquals("john.doe+test@example.com", response.email());

        ArgumentCaptor<CustomerCreateRequest> seedCaptor = ArgumentCaptor.forClass(CustomerCreateRequest.class);
        verify(createCustomerService).createWithCustomerId(
                eq(response.userId()),
                seedCaptor.capture(),
                eq("corr-1"),
                eq(response.userId()));

        CustomerCreateRequest seed = seedCaptor.getValue();
        assertEquals("John", seed.givenName());
        assertEquals("Doe", seed.familyName());
    }

    @Test
    void shouldGenerateCorrelationIdWhenMissing() {
        RegisterRequest request = new RegisterRequest("new.user@example.com", "Password123456");

        when(userAccountRepository.existsByEmailNormalized("new.user@example.com")).thenReturn(false);
        when(passwordHashService.hash("Password123456")).thenReturn("hash-1");
        when(userAccountRepository.save(any(UserAccountEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, UserAccountEntity.class));
        when(credentialRecordRepository.save(any(CredentialRecordEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, CredentialRecordEntity.class));
        when(authenticationEventRepository.save(any(AuthenticationEventEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, AuthenticationEventEntity.class));

        service.register(request, "   ");

        ArgumentCaptor<String> correlationCaptor = ArgumentCaptor.forClass(String.class);
        verify(createCustomerService).createWithCustomerId(any(), any(), correlationCaptor.capture(), any());
        assertNotNull(correlationCaptor.getValue());
        assertEquals(false, correlationCaptor.getValue().isBlank());
    }

    @Test
    void shouldMapCustomerDuplicateToAuthDuplicate() {
        RegisterRequest request = new RegisterRequest("alice@example.com", "Password123456");

        when(userAccountRepository.existsByEmailNormalized("alice@example.com")).thenReturn(false);
        when(passwordHashService.hash("Password123456")).thenReturn("hash-1");
        when(userAccountRepository.save(any(UserAccountEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, UserAccountEntity.class));
        when(credentialRecordRepository.save(any(CredentialRecordEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, CredentialRecordEntity.class));
        when(createCustomerService.createWithCustomerId(any(), any(), any(), any()))
                .thenThrow(new CustomerDomainException.DuplicateIdentityException("Duplicate"));

        DomainException ex = assertThrows(DomainException.class, () -> service.register(request, "corr-1"));

        assertEquals("AUTH-REG-002", ex.getErrorCode());
        assertEquals(409, ex.getStatus());
    }
}
