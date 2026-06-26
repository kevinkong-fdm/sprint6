package com.example.banking.customer.infrastructure;

import com.example.banking.customer.domain.CustomerProfileEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfileEntity, String> {
    boolean existsByEmailNormalized(String emailNormalized);

    Optional<CustomerProfileEntity> findByEmailNormalized(String emailNormalized);

    Optional<CustomerProfileEntity> findDetailedByCustomerId(String customerId);
}