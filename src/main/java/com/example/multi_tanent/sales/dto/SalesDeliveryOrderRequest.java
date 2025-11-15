package com.example.multi_tanent.sales.dto;

import com.example.multi_tanent.sales.enums.DocumentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class SalesDeliveryOrderRequest {
    private String number;
    private LocalDate date;
    private Long customerId;
    private DocumentStatus status;
    private String notes;
    private Long sourceSalesOrderId;
    private List<SalesDeliveryOrderLineRequest> lines;
}