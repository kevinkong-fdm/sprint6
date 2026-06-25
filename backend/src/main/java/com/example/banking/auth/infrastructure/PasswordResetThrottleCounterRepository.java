package com.example.banking.auth.infrastructure;

import com.example.banking.auth.domain.PasswordResetThrottleCounterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetThrottleCounterRepository extends JpaRepository<PasswordResetThrottleCounterEntity, String> {
}
