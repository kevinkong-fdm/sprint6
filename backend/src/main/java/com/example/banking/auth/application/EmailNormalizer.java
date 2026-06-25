package com.example.banking.auth.application;

import java.util.Locale;

public final class EmailNormalizer {
    private EmailNormalizer() {
    }

    public static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
