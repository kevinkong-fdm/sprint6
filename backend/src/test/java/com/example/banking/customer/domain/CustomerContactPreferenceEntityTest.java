package com.example.banking.customer.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CustomerContactPreferenceEntityTest {

    @Test
    void shouldCreatePreferenceAndNormalizeActor() {
        CustomerContactPreferenceEntity entity = CustomerContactPreferenceEntity.of(
                ContactChannel.EMAIL,
                true,
                " actor-1 ");

        assertNotNull(entity.getPreferenceId());
        assertEquals(ContactChannel.EMAIL, entity.getChannel());
        assertEquals(true, entity.isOptIn());
        assertEquals("actor-1", ReflectionTestUtils.getField(entity, "updatedBy"));
    }

    @Test
    void shouldDefaultNullActorToEmptyString() {
        CustomerContactPreferenceEntity entity = CustomerContactPreferenceEntity.of(ContactChannel.SMS, false, null);

        assertEquals("", ReflectionTestUtils.getField(entity, "updatedBy"));
    }
}
