package com.example.multi_tanent.tenant.payroll.dto;

import com.example.multi_tanent.tenant.payroll.enums.SalaryComponentType;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PayslipComponentResponse {
    private String componentName;
    private String componentCode;
    private SalaryComponentType componentType;
    private BigDecimal amount;
}