package com.example.banking.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EmailNormalizerTest {

    @Test
    void shouldNormalizeEmailToLowercaseTrimmedValue() {
        assertEquals("alice@example.com", EmailNormalizer.normalize(" Alice@Example.COM "));
        assertEquals("", EmailNormalizer.normalize(null));
    }
}
