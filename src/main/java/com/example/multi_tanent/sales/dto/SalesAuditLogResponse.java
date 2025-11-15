package com.example.multi_tanent.sales.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SalesAuditLogResponse {
    private Long id;
    private String entityName;
    private Long entityId;
    private String action;
    private String actor;
    private LocalDateTime timestamp;
    private String payloadJson;
}
