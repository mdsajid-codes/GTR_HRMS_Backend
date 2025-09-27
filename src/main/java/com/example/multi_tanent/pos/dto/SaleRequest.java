package com.example.multi_tanent.pos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SaleRequest {
    private Long customerId;

    @NotNull(message = "Store ID is required for a sale.")
    private Long storeId;
    private Long discountCents;

    @NotEmpty(message = "Sale must have at least one item.")
    @Valid
    private List<SaleItemRequest> items;

    @Valid
    private List<PaymentRequest> payments;
}