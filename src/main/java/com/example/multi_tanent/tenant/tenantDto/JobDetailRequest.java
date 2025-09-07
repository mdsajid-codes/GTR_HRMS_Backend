package com.example.multi_tanent.tenant.tenantDto;

import java.time.LocalDate;

import com.example.multi_tanent.tenant.entity.enums.EmployeeShift;
import com.example.multi_tanent.tenant.entity.enums.EmploymentType;
import com.example.multi_tanent.tenant.entity.enums.WorkMode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDetailRequest {
    private String departmentTitle;
    private String designationTitle;
    private EmploymentType employmentType;
    private WorkMode workMode;
    private LocalDate doj;
    private LocalDate endDate;
    private LocalDate probationEndDate;
    private Integer noticePeriodDay;
    private EmployeeShift shift;
}
