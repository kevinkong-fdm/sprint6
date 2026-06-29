package com.example.banking.account.application;

import com.example.banking.account.domain.AccountMovementEntity;
import com.example.banking.account.domain.MovementType;
import com.example.banking.account.infrastructure.AccountMovementRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class MovementIdempotencyService {

    private final AccountMovementRepository accountMovementRepository;

    public MovementIdempotencyService(AccountMovementRepository accountMovementRepository) {
        this.accountMovementRepository = accountMovementRepository;
    }

    public Optional<AccountMovementEntity> findExisting(String accountId, MovementType movementType, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }

        return accountMovementRepository.findFirstByAccountIdAndMovementTypeAndIdempotencyKeyOrderByCreatedAtDesc(
                accountId,
                movementType,
                idempotencyKey.trim());
    }
}
