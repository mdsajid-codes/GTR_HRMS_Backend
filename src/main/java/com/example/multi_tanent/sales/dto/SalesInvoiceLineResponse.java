package com.example.multi_tanent.sales.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SalesInvoiceLineResponse {
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
