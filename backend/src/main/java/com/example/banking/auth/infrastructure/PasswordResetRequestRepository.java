package com.example.banking.auth.infrastructure;

import com.example.banking.auth.domain.PasswordResetRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequestEntity, String> {
	@Transactional
	void deleteByUserId(String userId);
}
