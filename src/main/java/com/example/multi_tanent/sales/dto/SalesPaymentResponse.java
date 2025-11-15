package com.example.multi_tanent.sales.dto;

import com.example.multi_tanent.sales.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class SalesPaymentResponse {
    private Long id;
    private String number;
    private LocalDate date;
    private PaymentMethod method;
    private String reference;
    private BigDecimal amount;
    private String status;
    private Long customerId;
    private String customerName;
    private Long invoiceId;
    private String invoiceNumber;
}
