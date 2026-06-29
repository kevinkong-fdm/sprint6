package com.example.banking.account.application;

import com.example.banking.account.api.dto.AccountResponseMapper;
import com.example.banking.account.api.dto.TransactionHistoryItemResponse;
import com.example.banking.account.api.dto.TransactionHistoryResponse;
import com.example.banking.account.domain.AccountMovementEntity;
import com.example.banking.account.domain.MovementType;
import com.example.banking.account.infrastructure.AccountMovementRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class GetTransactionHistoryService {

    private final AccountAuthorizationService accountAuthorizationService;
    private final AccountMovementRepository accountMovementRepository;
    private final AccountResponseMapper accountResponseMapper;

    public GetTransactionHistoryService(
            AccountAuthorizationService accountAuthorizationService,
            AccountMovementRepository accountMovementRepository,
            AccountResponseMapper accountResponseMapper
    ) {
        this.accountAuthorizationService = accountAuthorizationService;
        this.accountMovementRepository = accountMovementRepository;
        this.accountResponseMapper = accountResponseMapper;
    }

    public TransactionHistoryResponse history(
            String accountId,
            String actorId,
            Instant from,
            Instant to,
            String movementType,
            int page,
            int size
    ) {
        accountAuthorizationService.requireOwnedAccount(accountId, actorId);

        if (from != null && to != null && from.isAfter(to)) {
            throw new AccountDomainException.HistoryValidationException();
        }

        MovementType resolvedType = null;
        if (movementType != null && !movementType.isBlank()) {
            try {
                resolvedType = MovementType.valueOf(movementType.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new AccountDomainException.HistoryValidationException();
            }
        }

        int resolvedPage = Math.max(1, page);
        int resolvedSize = Math.min(200, Math.max(1, size));
        PageRequest pageRequest = PageRequest.of(
                resolvedPage - 1,
                resolvedSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<AccountMovementEntity> specification = Specification.where(byAccountId(accountId));
        if (from != null) {
            specification = specification.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (to != null) {
            specification = specification.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }
        if (resolvedType != null) {
            MovementType finalResolvedType = resolvedType;
            specification = specification.and((root, query, cb) -> cb.equal(root.get("movementType"), finalResolvedType));
        }

        Page<AccountMovementEntity> result = accountMovementRepository.findAll(specification, pageRequest);
        List<TransactionHistoryItemResponse> items = new ArrayList<>();
        for (AccountMovementEntity entity : result.getContent()) {
            items.add(accountResponseMapper.toHistoryItem(entity));
        }

        return new TransactionHistoryResponse(accountId, items, resolvedPage, resolvedSize, result.getTotalElements());
    }

    private Specification<AccountMovementEntity> byAccountId(String accountId) {
        return (root, query, cb) -> cb.equal(root.get("accountId"), accountId);
    }
}
