package com.example.banking.auth.infrastructure;

import com.example.banking.auth.domain.CredentialRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface CredentialRecordRepository extends JpaRepository<CredentialRecordEntity, String> {
	@Transactional
	void deleteByUserId(String userId);
}
