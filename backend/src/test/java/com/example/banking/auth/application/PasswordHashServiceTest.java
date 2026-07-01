package com.example.banking.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHashServiceTest {

    private final PasswordHashService service = new PasswordHashService();

    @Test
    void shouldHashAndVerifyPasswords() {
        String hash = service.hash("Password123456");

        assertTrue(service.verify("Password123456", hash));
        assertEquals(false, service.verify("wrong", hash));
    }

    @Test
    void shouldVerifyBcryptPrefixedHashes() {
        String hash = service.hash("Password123456");

        assertTrue(service.verify("Password123456", "{bcrypt}" + hash));
        assertEquals(false, service.verify("Password123456", " "));
    }
}
