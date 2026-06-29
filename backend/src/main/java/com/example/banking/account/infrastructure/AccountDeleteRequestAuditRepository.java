package com.example.banking.account.infrastructure;

import com.example.banking.account.domain.AccountDeleteRequestAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountDeleteRequestAuditRepository extends JpaRepository<AccountDeleteRequestAuditEntity, String> {
}
