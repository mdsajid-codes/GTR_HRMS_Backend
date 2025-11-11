package com.example.multi_tanent.tenant.payroll.dto;

import com.example.multi_tanent.tenant.base.entity.CompanyInfo;
import com.example.multi_tanent.tenant.payroll.entity.EmployeeBenefitProvision;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitVoucherPdfData {
    private EmployeeBenefitProvision provision;
    private CompanyInfo companyInfo;
}