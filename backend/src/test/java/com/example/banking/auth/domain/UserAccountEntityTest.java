package com.example.banking.auth.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class UserAccountEntityTest {

    @Test
    void shouldCreateNewAccountWithDefaults() {
        UserAccountEntity entity = UserAccountEntity.newAccount("alice@example.com", "alice@example.com", "hash");

        assertNotNull(entity.getId());
        assertEquals("alice@example.com", entity.getEmail());
        assertEquals("alice@example.com", entity.getEmailNormalized());
        assertEquals("hash", entity.getPasswordHash());
        assertEquals(AccountStatus.ACTIVE, entity.getStatus());
        assertEquals(0, entity.getFailedLoginAttempts());
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    void shouldApplyMutatingSetters() {
        UserAccountEntity entity = UserAccountEntity.newAccount("alice@example.com", "alice@example.com", "hash");

        entity.setFailedLoginAttempts(2);
        entity.setStatus(AccountStatus.LOCKED);
        entity.setLockoutUntil(java.time.Instant.parse("2026-06-30T00:00:00Z"));
        entity.setPasswordHash("hash-2");
        entity.touchUpdatedAt();

        assertEquals(2, entity.getFailedLoginAttempts());
        assertEquals(AccountStatus.LOCKED, entity.getStatus());
        assertEquals("hash-2", entity.getPasswordHash());
        assertEquals(java.time.Instant.parse("2026-06-30T00:00:00Z"), entity.getLockoutUntil());
        assertNotNull(ReflectionTestUtils.getField(entity, "updatedAt"));
    }
}
