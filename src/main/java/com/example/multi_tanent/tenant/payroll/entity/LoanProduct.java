package com.example.multi_tanent.tenant.payroll.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

import java.time.LocalDate;

@Entity
@Table(name = "loan_products")
@Data
public class LoanProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String productName; // e.g., "General Purpose Loan", "Emergency Loan"

    private String description;

    @Column(precision = 5, scale = 2)
    private BigDecimal interestRate; // Annual interest rate

    private Integer maxInstallments;

    @Column(precision = 15, scale = 2)
    private BigDecimal maxLoanAmount;

    private boolean isActive;

    private LocalDate availabilityStartDate;

    private LocalDate availabilityEndDate;

    @Column(nullable = false)
    private boolean deductFromSalary = true;
}