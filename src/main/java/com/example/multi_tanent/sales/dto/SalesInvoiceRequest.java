package com.example.multi_tanent.sales.dto;

import com.example.multi_tanent.sales.enums.DocumentStatus;
import com.example.multi_tanent.sales.enums.InvoiceType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class SalesInvoiceRequest {
    private String number;
    private LocalDate date;
    private LocalDate dueDate;
    private Long customerId;
    private String currency;
    private DocumentStatus status;
    private InvoiceType type;
    private String notes;
    private Long sourceSalesOrderId;
    private List<SalesInvoiceLineRequest> lines;
    private Long termAndConditionId;
}