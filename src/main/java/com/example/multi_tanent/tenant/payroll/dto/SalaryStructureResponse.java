package com.example.multi_tanent.tenant.payroll.dto;

import com.example.multi_tanent.tenant.payroll.entity.SalaryStructure;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class SalaryStructureResponse {
    private Long id;
    private String employeeCode;
    private String structureName;
    private LocalDate effectiveDate;
    private List<SalaryStructureComponentResponse> components;

    public static SalaryStructureResponse fromEntity(SalaryStructure structure) {
        SalaryStructureResponse dto = new SalaryStructureResponse();
        dto.setId(structure.getId());
        dto.setEmployeeCode(structure.getEmployee().getEmployeeCode());
        dto.setStructureName(structure.getStructureName());
        dto.setEffectiveDate(structure.getEffectiveDate());
        dto.setComponents(structure.getComponents().stream()
                .map(SalaryStructureComponentResponse::fromEntity)
                .collect(Collectors.toList()));
        return dto;
    }
}