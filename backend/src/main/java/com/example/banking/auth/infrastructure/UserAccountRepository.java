package com.example.banking.auth.infrastructure;

import com.example.banking.auth.domain.UserAccountEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccountEntity, String> {
    boolean existsByEmailNormalized(String emailNormalized);

    Optional<UserAccountEntity> findByEmailNormalized(String emailNormalized);
}
