package com.example.banking.auth.infrastructure;

import com.example.banking.auth.domain.RefreshTokenSessionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSessionEntity, String> {
    Optional<RefreshTokenSessionEntity> findByTokenHash(String tokenHash);
}
