package com.example.banking.statement.infrastructure;

import com.example.banking.statement.domain.MonthlyStatementEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyStatementRepository extends JpaRepository<MonthlyStatementEntity, String> {

    Optional<MonthlyStatementEntity> findByAccountIdAndCustomerIdAndStatementMonth(
            String accountId,
            String customerId,
            String statementMonth
    );

        List<MonthlyStatementEntity> findByAccountIdAndCustomerIdOrderByStatementMonthDescGeneratedAtDesc(
            String accountId,
            String customerId
        );
}
