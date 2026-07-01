package com.example.banking.account.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.banking.account.domain.AccountType;
import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.infrastructure.BankAccountRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountAuthorizationServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    private AccountAuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new AccountAuthorizationService(bankAccountRepository);
    }

    @Test
    void shouldRequireActorId() {
        assertEquals("actor-1", service.requireActorId(" actor-1 "));
        assertThrows(AccountDomainException.AuthenticationRequiredException.class, () -> service.requireActorId("  "));
    }

    @Test
    void shouldRequireOwnedAccount() {
        BankAccountEntity account = BankAccountEntity.create("cust-1", AccountType.CHECKING, "Main");
        when(bankAccountRepository.findById(account.getAccountId())).thenReturn(Optional.of(account));

        BankAccountEntity resolved = service.requireOwnedAccount(account.getAccountId(), "cust-1");
        assertSame(account, resolved);

        assertThrows(AccountDomainException.AccessForbiddenException.class,
                () -> service.requireOwnedAccount(account.getAccountId(), "other"));
    }

    @Test
    void shouldThrowNotFoundWhenOwnedAccountMissing() {
        when(bankAccountRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(AccountDomainException.AccountNotFoundException.class,
                () -> service.requireOwnedAccount("missing", "cust-1"));
    }

    @Test
    void shouldRequireOwnedAccountForDelete() {
        BankAccountEntity account = BankAccountEntity.create("cust-1", AccountType.CHECKING, "Main");
        when(bankAccountRepository.findById(account.getAccountId())).thenReturn(Optional.of(account));

        BankAccountEntity resolved = service.requireOwnedAccountForDelete(account.getAccountId(), "cust-1");
        assertSame(account, resolved);

        assertThrows(AccountDomainException.AccessForbiddenException.class,
                () -> service.requireOwnedAccountForDelete(account.getAccountId(), "other"));
    }

    @Test
    void shouldThrowDeleteNotFoundWhenAccountMissing() {
        when(bankAccountRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(AccountDomainException.DeleteNotFoundException.class,
                () -> service.requireOwnedAccountForDelete("missing", "cust-1"));
    }
}
