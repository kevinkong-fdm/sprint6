package com.example.banking.insights.infrastructure;

import com.example.banking.insights.domain.SpendingInsightSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpendingInsightSnapshotRepository extends JpaRepository<SpendingInsightSnapshotEntity, String> {
}
