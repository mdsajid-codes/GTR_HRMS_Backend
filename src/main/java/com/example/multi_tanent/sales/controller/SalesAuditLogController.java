package com.example.multi_tanent.sales.controller;

import com.example.multi_tanent.sales.dto.SalesAuditLogResponse;
import com.example.multi_tanent.sales.service.SalesAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sales/audit-logs")
@RequiredArgsConstructor
public class SalesAuditLogController {

    private final SalesAuditLogService auditLogService;

    @GetMapping("/{entityName}/{entityId}")
    public ResponseEntity<List<SalesAuditLogResponse>> getLogsForEntity(@PathVariable String entityName, @PathVariable Long entityId) {
        return ResponseEntity.ok(auditLogService.getLogsForEntity(entityName, entityId));
    }
}
