package com.example.banking.auth.infrastructure;

import com.example.banking.auth.domain.PasswordResetRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequestEntity, String> {
}
