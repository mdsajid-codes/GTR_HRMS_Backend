package com.example.multi_tanent.tenant.payroll.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class SalaryStructureRequest {
    private String employeeCode;
    private String structureName;
    private LocalDate effectiveDate;
    private List<SalaryStructureComponentRequest> components;
}