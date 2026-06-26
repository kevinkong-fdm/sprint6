package com.example.banking.customer.infrastructure;

import com.example.banking.customer.domain.CustomerAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddressEntity, String> {
}
