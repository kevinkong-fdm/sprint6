package com.example.banking.account.application;

import com.example.banking.account.api.dto.AccountListResponse;
import com.example.banking.account.api.dto.AccountResponse;
import com.example.banking.account.api.dto.AccountResponseMapper;
import com.example.banking.account.domain.AccountType;
import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.infrastructure.BankAccountRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ListAccountsService {

    private final BankAccountRepository bankAccountRepository;
    private final AccountResponseMapper accountResponseMapper;

    public ListAccountsService(BankAccountRepository bankAccountRepository, AccountResponseMapper accountResponseMapper) {
        this.bankAccountRepository = bankAccountRepository;
        this.accountResponseMapper = accountResponseMapper;
    }

    public AccountListResponse list(String actorId, String accountTypeFilter, int page, int size) {
        int resolvedPage = Math.max(1, page);
        int resolvedSize = Math.min(200, Math.max(1, size));
        PageRequest pageRequest = PageRequest.of(resolvedPage - 1, resolvedSize);

        Page<BankAccountEntity> result;
        AccountType accountType = AccountType.from(accountTypeFilter);

        if (accountType == null) {
            result = bankAccountRepository.findByCustomerId(actorId, pageRequest);
        } else {
            result = bankAccountRepository.findByCustomerIdAndAccountType(actorId, accountType, pageRequest);
        }

        List<AccountResponse> items = result.stream().map(accountResponseMapper::toAccountResponse).toList();
        return accountResponseMapper.toAccountListResponse(items, resolvedPage, resolvedSize, result.getTotalElements());
    }
}
