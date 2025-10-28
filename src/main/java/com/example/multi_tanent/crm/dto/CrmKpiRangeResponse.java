package com.example.multi_tanent.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrmKpiRangeResponse {
    private Long id;
    private Double fromPercent;
    private Double toPercent;
    private String color;
}