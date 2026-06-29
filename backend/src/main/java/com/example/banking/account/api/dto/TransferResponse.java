package com.example.banking.account.api.dto;

public record TransferResponse(
        String transferId,
        MovementResponse sourceMovement,
        MovementResponse destinationMovement
) {
}
