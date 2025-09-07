package com.example.multi_tanent.tenant.tenantDto;

import java.time.LocalDate;

import com.example.multi_tanent.tenant.entity.enums.PayFrequency;
import com.example.multi_tanent.tenant.entity.enums.PayrollStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollRequest {
    private LocalDate payPeriodStart;
    private LocalDate payPeriodEnd;
    private PayFrequency payFrequency;
    private Double grossSalary;
    private Double netSalary;
    private Double basicSalary;
    private Double allowances;
    private Double deductions;
    private Double taxAmount;
    private String currency; 
    private PayrollStatus status;
    private String remarks;

}
