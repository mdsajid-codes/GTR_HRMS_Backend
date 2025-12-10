package com.example.multi_tanent.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BomSemiFinishedResponse {
    private Long id;
    private ProSemiFinishedResponse item;
    private String bomName;
    private boolean isLocked;
    private List<BomSemiFinishedDetailResponse> details;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
