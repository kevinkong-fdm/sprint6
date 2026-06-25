package com.example.banking.auth.application;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordHashService {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean verify(String rawPassword, String hash) {
        if (hash == null || hash.isBlank()) {
            return false;
        }

        // Support hashes stored either as raw bcrypt string or with {bcrypt} prefix.
        String normalizedHash = hash.startsWith("{bcrypt}") ? hash.substring("{bcrypt}".length()) : hash;
        return encoder.matches(rawPassword, normalizedHash);
    }
}