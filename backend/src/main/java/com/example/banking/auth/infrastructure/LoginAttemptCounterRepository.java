package com.example.banking.auth.infrastructure;

import com.example.banking.auth.domain.LoginAttemptCounterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAttemptCounterRepository extends JpaRepository<LoginAttemptCounterEntity, String> {
}
