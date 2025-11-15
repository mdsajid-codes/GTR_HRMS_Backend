package com.example.multi_tanent.sales.dto;

import com.example.multi_tanent.sales.enums.DocumentStatus;
import com.example.multi_tanent.sales.enums.InvoiceType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SalesInvoiceResponse {
    private Long id;
    private String number;
    private LocalDate date;
    private LocalDate dueDate;
    private Long customerId;
    private String customerName;
    private String currency;
    private DocumentStatus status;
    private InvoiceType type;
    private BigDecimal subtotal;
    private BigDecimal taxTotal;
    private BigDecimal grandTotal;
    private String notes;
    private Long sourceSalesOrderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
    private List<SalesInvoiceLineResponse> lines;
}