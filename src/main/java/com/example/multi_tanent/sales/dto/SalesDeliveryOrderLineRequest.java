package com.example.multi_tanent.sales.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SalesDeliveryOrderLineRequest {
    private Long productId;
    private String description;
    private BigDecimal quantity;
    private Long sourceOrderLineId;
}