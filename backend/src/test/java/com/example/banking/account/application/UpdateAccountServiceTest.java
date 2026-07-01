package com.example.banking.account.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.account.api.dto.AccountResponse;
import com.example.banking.account.api.dto.AccountResponseMapper;
import com.example.banking.account.api.dto.UpdateAccountRequest;
import com.example.banking.account.domain.AccountType;
import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.infrastructure.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateAccountServiceTest {

    @Mock
    private AccountAuthorizationService accountAuthorizationService;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private AccountResponseMapper accountResponseMapper;

    private UpdateAccountService service;

    @BeforeEach
    void setUp() {
        service = new UpdateAccountService(
                accountAuthorizationService,
                bankAccountRepository,
                accountResponseMapper);
    }

    @Test
    void shouldRejectImmutableFieldUpdateAttempt() {
        UpdateAccountRequest request = new UpdateAccountRequest("new name", "CHECKING", null, null);

        assertThrows(AccountDomainException.ImmutableFieldUpdateException.class,
                () -> service.update("acc-1", request, "cust-1"));
    }

    @Test
    void shouldRejectBlankNickname() {
        UpdateAccountRequest request = new UpdateAccountRequest("  ", null, null, null);

        assertThrows(AccountDomainException.UpdateValidationException.class,
                () -> service.update("acc-1", request, "cust-1"));
    }

    @Test
    void shouldUpdateNicknameAndMapResponse() {
        BankAccountEntity account = BankAccountEntity.create("cust-1", AccountType.CHECKING, "Old");

        when(accountAuthorizationService.requireOwnedAccount("acc-1", "cust-1")).thenReturn(account);
        when(bankAccountRepository.save(any(BankAccountEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, BankAccountEntity.class));

        AccountResponse mapped = new AccountResponse(
                "acc-1",
                "cust-1",
                "CHECKING",
                "New Name",
                "USD",
                "0.0000",
                "0.0000",
                "ACTIVE",
                account.getCreatedAt(),
                account.getUpdatedAt());
        when(accountResponseMapper.toAccountResponse(account)).thenReturn(mapped);

        AccountResponse response = service.update(
                "acc-1",
                new UpdateAccountRequest(" New Name ", null, null, null),
                "cust-1");

        assertEquals("New Name", account.getNickname());
        assertEquals("New Name", response.nickname());
        verify(bankAccountRepository).save(account);
    }
}
