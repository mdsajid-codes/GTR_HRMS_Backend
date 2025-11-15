package com.example.multi_tanent.sales.dto;

import com.example.multi_tanent.sales.enums.DocumentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SalesOrderResponse {
    private Long id;
    private String number;
    private LocalDate date;
    private Long customerId;
    private String customerName;
    private String currency;
    private DocumentStatus status;
    private BigDecimal subtotal;
    private BigDecimal taxTotal;
    private BigDecimal grandTotal;
    private String notes;
    private Long sourceQuotationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
    private List<SalesOrderLineResponse> lines;
}