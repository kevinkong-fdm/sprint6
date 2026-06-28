package com.example.banking.auth.infrastructure;

import com.example.banking.auth.domain.AccountStatus;
import com.example.banking.auth.domain.UserAccountEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccountEntity, String> {
    boolean existsByEmailNormalized(String emailNormalized);

    boolean existsByIdAndStatusNot(String id, AccountStatus status);

    Optional<UserAccountEntity> findByEmailNormalized(String emailNormalized);
}
