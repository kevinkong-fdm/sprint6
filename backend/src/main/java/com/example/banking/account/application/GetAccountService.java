package com.example.banking.account.application;

import com.example.banking.account.api.dto.AccountResponse;
import com.example.banking.account.api.dto.AccountResponseMapper;
import com.example.banking.account.domain.BankAccountEntity;
import org.springframework.stereotype.Service;

@Service
public class GetAccountService {

    private final AccountAuthorizationService accountAuthorizationService;
    private final AccountResponseMapper accountResponseMapper;

    public GetAccountService(AccountAuthorizationService accountAuthorizationService, AccountResponseMapper accountResponseMapper) {
        this.accountAuthorizationService = accountAuthorizationService;
        this.accountResponseMapper = accountResponseMapper;
    }

    public AccountResponse get(String accountId, String actorId) {
        BankAccountEntity account = accountAuthorizationService.requireOwnedAccount(accountId, actorId);
        return accountResponseMapper.toAccountResponse(account);
    }
}
