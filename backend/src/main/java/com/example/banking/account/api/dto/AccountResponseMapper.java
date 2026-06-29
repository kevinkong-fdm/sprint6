package com.example.banking.account.api.dto;

import com.example.banking.account.domain.AccountMovementEntity;
import com.example.banking.account.domain.BankAccountEntity;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AccountResponseMapper {

    public AccountResponse toAccountResponse(BankAccountEntity entity) {
        return new AccountResponse(
                entity.getAccountId(),
                entity.getCustomerId(),
                entity.getAccountType().name(),
                entity.getNickname(),
                entity.getCurrencyCode(),
            asMoney(entity.getAvailableBalance()),
            asMoney(entity.getLedgerBalance()),
                entity.getStatus().name(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public MovementResponse toMovementResponse(AccountMovementEntity entity) {
        return new MovementResponse(
                entity.getMovementId(),
                entity.getAccountId(),
                entity.getMovementType().name(),
            asMoney(entity.getAmount()),
            asMoney(entity.getBalanceAfter()),
                entity.getPostedAt());
    }

    public TransactionHistoryItemResponse toHistoryItem(AccountMovementEntity entity) {
        return new TransactionHistoryItemResponse(
                entity.getMovementId(),
                entity.getMovementType().name(),
            asMoney(entity.getAmount()),
            asMoney(entity.getBalanceAfter()),
                entity.getStatus().name(),
                entity.getCreatedAt(),
                entity.getPostedAt(),
                entity.getReferenceId());
    }

    public AccountListResponse toAccountListResponse(List<AccountResponse> items, int page, int size, long total) {
        return new AccountListResponse(items, page, size, total);
    }

    private String asMoney(java.math.BigDecimal value) {
        if (value == null) {
            return "0.0000";
        }
        return value.setScale(4, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
