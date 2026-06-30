package com.example.banking.account.infrastructure;

import com.example.banking.account.domain.AccountMovementEntity;
import com.example.banking.account.domain.MovementDirection;
import com.example.banking.account.domain.MovementStatus;
import com.example.banking.account.domain.MovementType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
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

    Optional<AccountMovementEntity> findTopByAccountIdAndPostedAtLessThanOrderByPostedAtDescMovementIdDesc(
            String accountId,
            Instant postedBefore
    );

    List<AccountMovementEntity> findByAccountIdAndPostedAtGreaterThanEqualAndPostedAtLessThanOrderByPostedAtAscMovementIdAsc(
            String accountId,
            Instant startInclusive,
            Instant endExclusive
    );

    List<AccountMovementEntity> findByAccountIdInAndDirectionAndPostedAtGreaterThanEqualAndPostedAtLessThan(
            Collection<String> accountIds,
            MovementDirection direction,
            Instant startInclusive,
            Instant endExclusive
    );
}
