package com.example.multi_tanent.tenant.payroll.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LoanProductRequest {
    private String productName;
    private String description;
    private BigDecimal interestRate;
    private Integer maxInstallments;
    private BigDecimal maxLoanAmount;
    private boolean isActive;
}