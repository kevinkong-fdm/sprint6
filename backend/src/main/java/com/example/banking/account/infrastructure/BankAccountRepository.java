package com.example.banking.account.infrastructure;

import com.example.banking.account.domain.AccountType;
import com.example.banking.account.domain.BankAccountEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccountEntity, String> {

    Optional<BankAccountEntity> findByAccountIdAndCustomerId(String accountId, String customerId);

    List<BankAccountEntity> findByCustomerId(String customerId);

    Page<BankAccountEntity> findByCustomerId(String customerId, Pageable pageable);

    Page<BankAccountEntity> findByCustomerIdAndAccountType(String customerId, AccountType accountType, Pageable pageable);
}
