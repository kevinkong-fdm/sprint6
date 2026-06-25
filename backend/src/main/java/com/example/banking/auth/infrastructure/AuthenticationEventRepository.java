package com.example.banking.auth.infrastructure;

import com.example.banking.auth.domain.AuthenticationEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthenticationEventRepository extends JpaRepository<AuthenticationEventEntity, String> {
}
