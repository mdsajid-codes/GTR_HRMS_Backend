package com.example.multi_tanent.sales.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SaleProductResponse {

    private Long id;

    private String sku;

    private String name;

    private String description;

    private String uom;

    private BigDecimal taxRate;

    private BigDecimal unitPrice;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}