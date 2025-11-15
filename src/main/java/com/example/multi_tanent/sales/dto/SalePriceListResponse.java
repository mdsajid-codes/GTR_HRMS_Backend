package com.example.multi_tanent.sales.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SalePriceListResponse {
    private Long id;
    private String name;
    private String currency;
    private boolean isDefault;
    private List<SalePriceListItemResponse> items;

    // From AbstractAuditable
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
}
