package com.example.multi_tanent.sales.dto;

import com.example.multi_tanent.sales.enums.DocumentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class SalesOrderRequest {
    private String number;
    private LocalDate date;
    private Long customerId;
    private String currency;
    private DocumentStatus status;
    private String notes;
    private Long sourceQuotationId;
    private List<SalesOrderLineRequest> lines;
    private Long termAndConditionId;
}