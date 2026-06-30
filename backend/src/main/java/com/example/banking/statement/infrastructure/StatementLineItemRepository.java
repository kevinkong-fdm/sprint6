package com.example.banking.statement.infrastructure;

import com.example.banking.statement.domain.StatementLineItemEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatementLineItemRepository extends JpaRepository<StatementLineItemEntity, String> {

    List<StatementLineItemEntity> findByMonthlyStatementIdOrderByPostedAtAscTransactionIdAsc(String monthlyStatementId);

    void deleteByMonthlyStatementId(String monthlyStatementId);
}
