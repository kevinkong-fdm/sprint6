package com.example.banking.account.application;

import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.infrastructure.BankAccountRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountAuthorizationService {

    private final BankAccountRepository bankAccountRepository;

    public AccountAuthorizationService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    public String requireActorId(String actorId) {
        if (actorId == null || actorId.isBlank()) {
            throw new AccountDomainException.AuthenticationRequiredException();
        }
        return actorId.trim();
    }

    public BankAccountEntity requireOwnedAccount(String accountId, String actorId) {
        BankAccountEntity account = bankAccountRepository.findById(accountId)
                .orElseThrow(AccountDomainException.AccountNotFoundException::new);

        if (!account.getCustomerId().equals(actorId)) {
            throw new AccountDomainException.AccessForbiddenException();
        }

        return account;
    }

    public BankAccountEntity requireOwnedAccountForDelete(String accountId, String actorId) {
        return bankAccountRepository.findById(accountId)
                .map(account -> {
                    if (!account.getCustomerId().equals(actorId)) {
                        throw new AccountDomainException.AccessForbiddenException();
                    }
                    return account;
                })
                .orElseThrow(AccountDomainException.DeleteNotFoundException::new);
    }
}
