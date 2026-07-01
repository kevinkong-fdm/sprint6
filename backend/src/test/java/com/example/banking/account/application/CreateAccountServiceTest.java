package com.example.banking.account.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.account.api.dto.AccountResponse;
import com.example.banking.account.api.dto.AccountResponseMapper;
import com.example.banking.account.api.dto.CreateAccountRequest;
import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.infrastructure.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateAccountServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private AccountResponseMapper accountResponseMapper;

    private CreateAccountService service;

    @BeforeEach
    void setUp() {
        service = new CreateAccountService(bankAccountRepository, accountResponseMapper);
    }

    @Test
    void shouldRejectUnsupportedAccountType() {
        assertThrows(
                AccountDomainException.UnsupportedAccountTypeException.class,
                () -> service.create(new CreateAccountRequest("invalid", "Main"), "cust-1"));
    }

    @Test
    void shouldCreateAndMapAccount() {
        when(bankAccountRepository.save(any(BankAccountEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, BankAccountEntity.class));
        when(accountResponseMapper.toAccountResponse(any(BankAccountEntity.class))).thenAnswer(invocation -> {
            BankAccountEntity entity = invocation.getArgument(0, BankAccountEntity.class);
            return new AccountResponse(
                    entity.getAccountId(),
                    entity.getCustomerId(),
                    entity.getAccountType().name(),
                    entity.getNickname(),
                    entity.getCurrencyCode(),
                    entity.getAvailableBalance().toPlainString(),
                    entity.getLedgerBalance().toPlainString(),
                    entity.getStatus().name(),
                    entity.getCreatedAt(),
                    entity.getUpdatedAt());
        });

        AccountResponse response = service.create(new CreateAccountRequest("checking", " Main "), "cust-1");

        assertEquals("cust-1", response.customerId());
        assertEquals("CHECKING", response.accountType());
        assertEquals("Main", response.nickname());
        verify(bankAccountRepository).save(any(BankAccountEntity.class));
    }
}
