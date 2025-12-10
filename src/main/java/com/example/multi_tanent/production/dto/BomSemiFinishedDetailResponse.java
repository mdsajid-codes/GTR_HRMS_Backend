package com.example.multi_tanent.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BomSemiFinishedDetailResponse {
    private Long id;
    private ProProcessResponse process;
    private ProRawMaterialsResponse rawMaterial;
    private ProSemiFinishedResponse childSemiFinished;
    private BigDecimal quantity;
    private String notes;
    private Integer sequence;
}
