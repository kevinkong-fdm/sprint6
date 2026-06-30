package com.example.banking.standingorder.api.dto;

import jakarta.validation.constraints.Size;

public record TriggerExecutionRequest(
        @Size(max = 300) String reason
) {
}
