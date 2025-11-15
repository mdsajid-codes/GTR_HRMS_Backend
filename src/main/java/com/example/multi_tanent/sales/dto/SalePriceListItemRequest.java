package com.example.multi_tanent.sales.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class SalePriceListItemRequest {
    private Long productId;
    private BigDecimal unitPrice;
    private LocalDate validFrom;
    private LocalDate validTo;
}