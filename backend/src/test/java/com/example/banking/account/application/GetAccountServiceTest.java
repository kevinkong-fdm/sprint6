package com.example.banking.account.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.account.api.dto.AccountResponse;
import com.example.banking.account.api.dto.AccountResponseMapper;
import com.example.banking.account.domain.AccountType;
import com.example.banking.account.domain.BankAccountEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAccountServiceTest {

    @Mock
    private AccountAuthorizationService accountAuthorizationService;

    @Mock
    private AccountResponseMapper accountResponseMapper;

    private GetAccountService service;

    @BeforeEach
    void setUp() {
        service = new GetAccountService(accountAuthorizationService, accountResponseMapper);
    }

    @Test
    void shouldResolveOwnedAccountAndMapResponse() {
        BankAccountEntity account = BankAccountEntity.create("cust-1", AccountType.CHECKING, "Main");
        AccountResponse mapped = new AccountResponse(
                account.getAccountId(),
                account.getCustomerId(),
                account.getAccountType().name(),
                account.getNickname(),
                account.getCurrencyCode(),
                account.getAvailableBalance().toPlainString(),
                account.getLedgerBalance().toPlainString(),
                account.getStatus().name(),
                account.getCreatedAt(),
                account.getUpdatedAt());

        when(accountAuthorizationService.requireOwnedAccount("acc-1", "cust-1")).thenReturn(account);
        when(accountResponseMapper.toAccountResponse(account)).thenReturn(mapped);

        AccountResponse response = service.get("acc-1", "cust-1");

        assertEquals(account.getAccountId(), response.accountId());
        verify(accountAuthorizationService).requireOwnedAccount("acc-1", "cust-1");
    }
}
