package com.example.multi_tanent.sales.dto;

import com.example.multi_tanent.sales.entity.SalesTermAndCondition;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SalesTermAndConditionResponse {
    private Long id;
    private String name;
    private String content;
    private boolean isDefault;

    public static SalesTermAndConditionResponse fromEntity(SalesTermAndCondition entity) {
        if (entity == null) return null;

        return SalesTermAndConditionResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .content(entity.getContent())
                .isDefault(entity.isDefault())
                .build();
    }
}