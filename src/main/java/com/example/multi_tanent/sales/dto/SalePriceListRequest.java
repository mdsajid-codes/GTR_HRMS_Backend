package com.example.multi_tanent.sales.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SalePriceListRequest {
    private String name;
    private String currency;
    private boolean isDefault;
    private List<SalePriceListItemRequest> items;
}
