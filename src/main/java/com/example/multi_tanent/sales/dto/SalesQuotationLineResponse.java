package com.example.multi_tanent.sales.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SalesQuotationLineResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private Long unitId;
    private String unitName;
    private BigDecimal discount;
    private Long taxId;
    private String taxCode;
    private BigDecimal taxRate; // The rate from the ProTax entity
    private BigDecimal lineTotal;
}