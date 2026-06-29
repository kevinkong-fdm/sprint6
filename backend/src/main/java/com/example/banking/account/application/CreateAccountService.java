package com.example.banking.account.application;

import com.example.banking.account.api.dto.AccountResponse;
import com.example.banking.account.api.dto.AccountResponseMapper;
import com.example.banking.account.api.dto.CreateAccountRequest;
import com.example.banking.account.domain.AccountType;
import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.infrastructure.BankAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final AccountResponseMapper accountResponseMapper;

    public CreateAccountService(BankAccountRepository bankAccountRepository, AccountResponseMapper accountResponseMapper) {
        this.bankAccountRepository = bankAccountRepository;
        this.accountResponseMapper = accountResponseMapper;
    }

    @Transactional
    public AccountResponse create(CreateAccountRequest request, String actorId) {
        AccountType accountType = AccountType.from(request.accountType());
        if (accountType == null) {
            throw new AccountDomainException.UnsupportedAccountTypeException();
        }

        BankAccountEntity saved = bankAccountRepository.save(BankAccountEntity.create(actorId, accountType, request.nickname()));
        return accountResponseMapper.toAccountResponse(saved);
    }
}
