package com.example.banking.account.application;

import com.example.banking.account.api.dto.AccountResponse;
import com.example.banking.account.api.dto.AccountResponseMapper;
import com.example.banking.account.api.dto.UpdateAccountRequest;
import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.infrastructure.BankAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateAccountService {

    private final AccountAuthorizationService accountAuthorizationService;
    private final BankAccountRepository bankAccountRepository;
    private final AccountResponseMapper accountResponseMapper;

    public UpdateAccountService(
            AccountAuthorizationService accountAuthorizationService,
            BankAccountRepository bankAccountRepository,
            AccountResponseMapper accountResponseMapper
    ) {
        this.accountAuthorizationService = accountAuthorizationService;
        this.bankAccountRepository = bankAccountRepository;
        this.accountResponseMapper = accountResponseMapper;
    }

    @Transactional
    public AccountResponse update(String accountId, UpdateAccountRequest request, String actorId) {
        if (hasImmutableFieldAttempt(request)) {
            throw new AccountDomainException.ImmutableFieldUpdateException();
        }

        if (request.nickname() == null || request.nickname().trim().isBlank()) {
            throw new AccountDomainException.UpdateValidationException("Account update validation failed.");
        }

        BankAccountEntity account = accountAuthorizationService.requireOwnedAccount(accountId, actorId);
        account.updateNickname(request.nickname());
        BankAccountEntity saved = bankAccountRepository.save(account);
        return accountResponseMapper.toAccountResponse(saved);
    }

    private boolean hasImmutableFieldAttempt(UpdateAccountRequest request) {
        return hasText(request.accountType())
                || hasText(request.currencyCode())
                || hasText(request.status());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
