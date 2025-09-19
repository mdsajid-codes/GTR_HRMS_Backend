package com.example.multi_tanent.tenant.payroll.dto;

import lombok.Data;

@Data
public class PayrollSettingRequest {
    private String payFrequency;
    private Integer payCycleDay;
    private Integer payslipGenerationDay;
    private boolean includeHolidaysInPayslip;
    private boolean includeLeaveBalanceInPayslip;
}