package com.example.multi_tanent.tenant.payroll.dto;

import com.example.multi_tanent.tenant.payroll.entity.PayslipTemplate;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class PayslipTemplateResponse {
    private Long id;
    private String name;
    private String templateContent;
    private boolean isDefault;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static PayslipTemplateResponse fromEntity(PayslipTemplate entity) {
        return PayslipTemplateResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .templateContent(entity.getTemplateContent())
                .isDefault(entity.isDefault())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}