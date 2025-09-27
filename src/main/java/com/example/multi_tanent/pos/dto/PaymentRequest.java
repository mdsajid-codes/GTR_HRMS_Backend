package com.example.multi_tanent.pos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotBlank(message = "Payment method is required.")
    private String method; // e.g., cash, card

    @NotNull(message = "Payment amount is required.")
    @Min(value = 0, message = "Amount cannot be negative.")
    private Long amountCents;

    private String reference; // e.g., transaction ID
}