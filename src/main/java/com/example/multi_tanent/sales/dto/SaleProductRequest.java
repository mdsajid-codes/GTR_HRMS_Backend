package com.example.multi_tanent.sales.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SaleProductRequest {

    private String sku;

    private String name;

    private String description;

    private String uom;

    private BigDecimal taxRate;

    private BigDecimal unitPrice;

    private String status;
}