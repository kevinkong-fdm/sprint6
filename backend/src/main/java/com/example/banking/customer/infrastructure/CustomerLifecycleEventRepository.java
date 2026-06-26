package com.example.banking.customer.infrastructure;

import com.example.banking.customer.domain.CustomerLifecycleEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerLifecycleEventRepository extends JpaRepository<CustomerLifecycleEventEntity, String> {
}
