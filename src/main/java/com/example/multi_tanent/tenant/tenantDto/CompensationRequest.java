package com.example.multi_tanent.tenant.tenantDto;

import com.example.multi_tanent.tenant.entity.enums.ComponentType;
import com.example.multi_tanent.tenant.entity.enums.TaxTreatment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompensationRequest {
    private ComponentType componentType;
    private Double amount;
    private Double percentOfBasic;
    private TaxTreatment taxTreatment;
}
