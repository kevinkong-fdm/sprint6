package com.example.banking.auth.infrastructure;

import com.example.banking.auth.domain.CredentialRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialRecordRepository extends JpaRepository<CredentialRecordEntity, String> {
}
