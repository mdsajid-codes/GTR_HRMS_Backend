package com.example.multi_tanent.crm.dto;


import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CrmKpiRangeRequest {
    @NotNull(message = "KPI ID is required")
    private Long kpiId;

    private Long locationId; // Optional
    private Double fromPercent;
    private Double toPercent;
    private String color;
}
