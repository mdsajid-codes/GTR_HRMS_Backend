package com.example.multi_tanent.sales.dto;

import com.example.multi_tanent.sales.enums.DocumentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SalesDeliveryOrderResponse {
    private Long id;
    private String number;
    private LocalDate date;
    private Long customerId;
    private String customerName;
    private DocumentStatus status;
    private String notes;
    private Long sourceSalesOrderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
    private List<SalesDeliveryOrderLineResponse> lines;
}