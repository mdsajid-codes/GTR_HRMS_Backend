package com.example.multi_tanent.tenant.payroll.dto;

import com.example.multi_tanent.tenant.payroll.entity.SalaryComponent;
import com.example.multi_tanent.tenant.payroll.enums.CalculationType;
import com.example.multi_tanent.tenant.payroll.enums.SalaryComponentType;
import lombok.Data;

@Data
public class SalaryComponentResponse {
    private Long id;
    private String name;
    private String code;
    private SalaryComponentType type;
    private CalculationType calculationType;
    private String formula;
    private boolean isTaxable;
    private boolean partOfGrossSalary;
    private Integer displayOrder;

    public static SalaryComponentResponse fromEntity(SalaryComponent component) {
        if (component == null) return null;
        SalaryComponentResponse dto = new SalaryComponentResponse();
        dto.setId(component.getId());
        dto.setName(component.getName());
        dto.setCode(component.getCode());
        dto.setType(component.getType());
        dto.setCalculationType(component.getCalculationType());
        dto.setFormula(component.getFormula());
        dto.setTaxable(component.isTaxable());
        dto.setPartOfGrossSalary(component.isPartOfGrossSalary());
        dto.setDisplayOrder(component.getDisplayOrder());
        return dto;
    }
}