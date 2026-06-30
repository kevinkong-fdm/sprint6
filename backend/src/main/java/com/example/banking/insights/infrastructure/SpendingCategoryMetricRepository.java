package com.example.banking.insights.infrastructure;

import com.example.banking.insights.domain.SpendingCategoryMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpendingCategoryMetricRepository extends JpaRepository<SpendingCategoryMetricEntity, String> {
}
