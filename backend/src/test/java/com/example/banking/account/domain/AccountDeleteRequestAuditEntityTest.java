package com.example.banking.account.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AccountDeleteRequestAuditEntityTest {

    @Test
    void shouldCreateAuditEntityWithNormalizedValues() {
        AccountDeleteRequestAuditEntity entity = AccountDeleteRequestAuditEntity.of(
                " acc-1 ",
                " actor-1 ",
                "   ",
                DeleteEligibilityResult.ALLOWED,
                " ",
                null);

        assertNotNull(ReflectionTestUtils.getField(entity, "deleteAuditId"));
        assertEquals("acc-1", ReflectionTestUtils.getField(entity, "accountId"));
        assertEquals("actor-1", ReflectionTestUtils.getField(entity, "actorCustomerId"));
        assertEquals(null, ReflectionTestUtils.getField(entity, "requestedCloseoutDestinationAccountId"));
        assertEquals(DeleteEligibilityResult.ALLOWED, ReflectionTestUtils.getField(entity, "eligibilityResult"));
        assertEquals(null, ReflectionTestUtils.getField(entity, "errorCode"));
        assertEquals("", ReflectionTestUtils.getField(entity, "correlationId"));
        assertNotNull(ReflectionTestUtils.getField(entity, "createdAt"));
    }
}
