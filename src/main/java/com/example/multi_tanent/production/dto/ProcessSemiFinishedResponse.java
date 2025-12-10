package com.example.multi_tanent.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessSemiFinishedResponse {
    private Long id;
    private Long itemId;
    private String itemName;
    private String processFlowName;
    private BigDecimal otherFixedCost;
    private BigDecimal otherVariableCost;
    private boolean isLocked;
    private List<ProcessSemiFinishedDetailResponse> details;
    private Long locationId;
    private String locationName;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
