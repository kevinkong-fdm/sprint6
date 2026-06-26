package com.example.banking.customer.application;

import java.util.Locale;

public final class CustomerEmailNormalizer {
    private CustomerEmailNormalizer() {
    }

    public static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
