package com.example.multi_tanent.sales.dto;

import com.example.multi_tanent.sales.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class SalesPaymentRequest {
    private String number;
    private LocalDate date;
    private PaymentMethod method;
    private String reference;
    private BigDecimal amount;
    private String status;
    private Long customerId;
    private Long invoiceId;
}
