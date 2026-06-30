package com.example.banking.standingorder.infrastructure;

import com.example.banking.standingorder.domain.StandingOrderExecutionEntity;
import com.example.banking.standingorder.domain.StandingOrderExecutionOutcome;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StandingOrderExecutionRepository extends JpaRepository<StandingOrderExecutionEntity, String> {

    Optional<StandingOrderExecutionEntity> findFirstByStandingOrderIdAndIdempotencyKeyOrderByCreatedAtDesc(
            String standingOrderId,
            String idempotencyKey
    );

    Page<StandingOrderExecutionEntity> findByStandingOrderId(String standingOrderId, Pageable pageable);

    Page<StandingOrderExecutionEntity> findByStandingOrderIdAndOutcome(
            String standingOrderId,
            StandingOrderExecutionOutcome outcome,
            Pageable pageable
    );
}
