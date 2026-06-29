package com.example.banking.account.infrastructure;

import com.example.banking.account.domain.AccountMovementEntity;
import com.example.banking.account.domain.MovementStatus;
import com.example.banking.account.domain.MovementType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AccountMovementRepository extends JpaRepository<AccountMovementEntity, String>, JpaSpecificationExecutor<AccountMovementEntity> {

    boolean existsByAccountIdAndStatus(String accountId, MovementStatus status);

    Optional<AccountMovementEntity> findFirstByAccountIdAndMovementTypeAndIdempotencyKeyOrderByCreatedAtDesc(
            String accountId,
            MovementType movementType,
            String idempotencyKey
    );

    Optional<AccountMovementEntity> findFirstByReferenceIdAndMovementTypeOrderByCreatedAtDesc(
            String referenceId,
            MovementType movementType
    );
}
