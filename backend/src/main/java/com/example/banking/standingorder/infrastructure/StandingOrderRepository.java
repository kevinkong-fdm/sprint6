package com.example.banking.standingorder.infrastructure;

import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.domain.StandingOrderStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StandingOrderRepository extends JpaRepository<StandingOrderEntity, String> {

    Optional<StandingOrderEntity> findByStandingOrderIdAndCustomerId(String standingOrderId, String customerId);

    Page<StandingOrderEntity> findByCustomerId(String customerId, Pageable pageable);

    Page<StandingOrderEntity> findByCustomerIdAndStatus(String customerId, StandingOrderStatus status, Pageable pageable);

    Optional<StandingOrderEntity> findFirstByCustomerIdAndIdempotencyKeyOrderByCreatedAtDesc(String customerId, String idempotencyKey);
}
