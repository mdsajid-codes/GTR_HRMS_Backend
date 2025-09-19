package com.example.multi_tanent.tenant.payroll.dto;

import com.example.multi_tanent.tenant.payroll.enums.CalculationType;
import com.example.multi_tanent.tenant.payroll.enums.SalaryComponentType;
import lombok.Data;

@Data
public class SalaryComponentRequest {
    private String name;
    private String code;
    private SalaryComponentType type;
    private CalculationType calculationType;
    private String formula;
    private boolean isTaxable;
    private boolean partOfGrossSalary;
    private Integer displayOrder;
}