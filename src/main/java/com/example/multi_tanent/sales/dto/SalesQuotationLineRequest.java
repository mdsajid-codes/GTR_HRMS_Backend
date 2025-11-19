package com.example.multi_tanent.sales.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SalesQuotationLineRequest {
    private Long productId;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private Long unitId;
    private Long taxId;
}