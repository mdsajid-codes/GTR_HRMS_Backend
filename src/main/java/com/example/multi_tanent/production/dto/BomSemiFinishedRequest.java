package com.example.multi_tanent.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BomSemiFinishedRequest {
    private Long itemId;
    private String bomName;
    private boolean isLocked;
    private List<BomSemiFinishedDetailRequest> details;
}
