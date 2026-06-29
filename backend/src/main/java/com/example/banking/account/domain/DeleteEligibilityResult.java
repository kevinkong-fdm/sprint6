package com.example.banking.account.domain;

public enum DeleteEligibilityResult {
    ALLOWED,
    REJECTED_PENDING_MOVEMENT,
    REJECTED_INVALID_DESTINATION,
    REJECTED_UNAUTHORIZED
}
