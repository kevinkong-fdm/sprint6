package com.example.banking.statement.infrastructure;

import com.example.banking.statement.domain.MonthlyStatementEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyStatementRepository extends JpaRepository<MonthlyStatementEntity, String> {

    Optional<MonthlyStatementEntity> findByAccountIdAndCustomerIdAndStatementMonth(
            String accountId,
            String customerId,
            String statementMonth
    );
}
