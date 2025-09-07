package com.example.multi_tanent.tenant.tenantDto;

import com.example.multi_tanent.tenant.entity.enums.PayFrequency;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryDetailRequest {
    private PayFrequency payFrequency;
    private Double ctcAnnual;
    private Boolean bonusEligible;
    private Double bonusTargetPct;
    private String currency;
}
