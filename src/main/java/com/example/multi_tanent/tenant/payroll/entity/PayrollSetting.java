package com.example.multi_tanent.tenant.payroll.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "payroll_settings")
@Data
public class PayrollSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Should only be one record per tenant

    @Column(nullable = false)
    private String payFrequency; // e.g., MONTHLY, BI_WEEKLY

    @Column(nullable = false)
    private Integer payCycleDay; // e.g., 1 for 1st of the month

    @Column(nullable = false)
    private Integer payslipGenerationDay; // Day of the month to generate payslips

    private boolean includeHolidaysInPayslip;
    private boolean includeLeaveBalanceInPayslip;
}