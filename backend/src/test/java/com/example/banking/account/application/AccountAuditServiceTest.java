package com.example.banking.account.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.account.domain.AccountDeleteRequestAuditEntity;
import com.example.banking.account.domain.DeleteEligibilityResult;
import com.example.banking.account.infrastructure.AccountDeleteRequestAuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AccountAuditServiceTest {

    @Mock
    private AccountDeleteRequestAuditRepository accountDeleteRequestAuditRepository;

    private AccountAuditService service;

    @BeforeEach
    void setUp() {
        service = new AccountAuditService(accountDeleteRequestAuditRepository);
    }

    @Test
    void shouldPersistDeleteDecisionAudit() {
        when(accountDeleteRequestAuditRepository.save(org.mockito.ArgumentMatchers.any(AccountDeleteRequestAuditEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, AccountDeleteRequestAuditEntity.class));

        service.recordDeleteDecision(
                "acc-1",
                "cust-1",
                "dst-1",
                DeleteEligibilityResult.ALLOWED,
                null,
                "corr-1");

        ArgumentCaptor<AccountDeleteRequestAuditEntity> captor = ArgumentCaptor.forClass(AccountDeleteRequestAuditEntity.class);
        verify(accountDeleteRequestAuditRepository).save(captor.capture());

        AccountDeleteRequestAuditEntity saved = captor.getValue();
        assertEquals("acc-1", ReflectionTestUtils.getField(saved, "accountId"));
        assertEquals("cust-1", ReflectionTestUtils.getField(saved, "actorCustomerId"));
        assertEquals(DeleteEligibilityResult.ALLOWED, ReflectionTestUtils.getField(saved, "eligibilityResult"));
        assertNotNull(ReflectionTestUtils.getField(saved, "createdAt"));
    }
}
