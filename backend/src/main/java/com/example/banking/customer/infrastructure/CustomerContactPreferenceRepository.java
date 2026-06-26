package com.example.banking.customer.infrastructure;

import com.example.banking.customer.domain.CustomerContactPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerContactPreferenceRepository extends JpaRepository<CustomerContactPreferenceEntity, String> {
}
