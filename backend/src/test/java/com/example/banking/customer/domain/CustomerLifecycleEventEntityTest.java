package com.example.banking.customer.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CustomerLifecycleEventEntityTest {

    @Test
    void shouldCreateLifecycleEventWithNormalizedFields() {
        CustomerLifecycleEventEntity entity = CustomerLifecycleEventEntity.of(
                " cust-1 ",
                LifecycleAction.CREATE,
                LifecycleOutcome.SUCCESS,
                "   ",
                "   ",
                null,
                null);

        assertNotNull(ReflectionTestUtils.getField(entity, "eventId"));
        assertEquals("cust-1", ReflectionTestUtils.getField(entity, "customerId"));
        assertEquals(LifecycleAction.CREATE, ReflectionTestUtils.getField(entity, "action"));
        assertEquals(LifecycleOutcome.SUCCESS, ReflectionTestUtils.getField(entity, "outcome"));
        assertEquals(null, ReflectionTestUtils.getField(entity, "errorCode"));
        assertEquals("unknown", ReflectionTestUtils.getField(entity, "actorId"));
        assertEquals("unknown", ReflectionTestUtils.getField(entity, "correlationId"));
        assertEquals("{}", ReflectionTestUtils.getField(entity, "metadataJson"));
        assertNotNull(ReflectionTestUtils.getField(entity, "occurredAt"));
    }
}
