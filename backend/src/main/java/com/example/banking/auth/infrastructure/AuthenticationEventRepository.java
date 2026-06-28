package com.example.banking.auth.infrastructure;

import com.example.banking.auth.domain.AuthenticationEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface AuthenticationEventRepository extends JpaRepository<AuthenticationEventEntity, String> {
	@Transactional
	void deleteByUserId(String userId);
}
