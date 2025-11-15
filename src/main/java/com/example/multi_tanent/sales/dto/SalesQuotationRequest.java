package com.example.multi_tanent.sales.dto;

import com.example.multi_tanent.sales.enums.DocumentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class SalesQuotationRequest {
    private String number;
    private LocalDate date;
    private LocalDate expiryDate;
    private Long customerId;
    private String currency;
    private DocumentStatus status;
    private String notes;
    private List<SalesQuotationLineRequest> items;
}