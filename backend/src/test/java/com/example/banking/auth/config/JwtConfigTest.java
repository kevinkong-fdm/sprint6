package com.example.banking.auth.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class JwtConfigTest {

    @Test
    void shouldInstantiateConfigAndPropertiesRecord() {
        JwtConfig config = new JwtConfig();
        JwtProperties properties = new JwtProperties("issuer", "secret", 60, 120);

        assertNotNull(config);
        assertNotNull(properties);
        assertNotNull(properties.secret());
    }
}
