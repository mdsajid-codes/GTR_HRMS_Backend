package com.example.multi_tanent.sales.service;

import com.example.multi_tanent.sales.dto.SalesAuditLogResponse;
import com.example.multi_tanent.sales.entity.SalesAuditLog;
import com.example.multi_tanent.sales.repository.SalesAuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SalesAuditLogService {

    private final SalesAuditLogRepository auditLogRepo;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    /**
     * Creates an audit log entry. This method is intended to be called by other services.
     *
     * @param entityName The name of the entity being audited (e.g., "SalesOrder").
     * @param entityId   The ID of the entity instance.
     * @param action     The action performed (e.g., "CREATE", "UPDATE_STATUS").
     * @param actor      The user or system performing the action.
     * @param payload    The object to be serialized as JSON, representing the state or change.
     */
    public void createLog(String entityName, Long entityId, String action, String actor, Object payload) {
        SalesAuditLog logEntry = new SalesAuditLog();
        logEntry.setEntityName(entityName);
        logEntry.setEntityId(entityId);
        logEntry.setAction(action);
        logEntry.setActor(actor != null ? actor : "System");

        try {
            logEntry.setPayloadJson(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            log.error("Error serializing audit log payload for entity {} with ID {}", entityName, entityId, e);
            logEntry.setPayloadJson("{\"error\":\"Failed to serialize payload\"}");
        }

        auditLogRepo.save(logEntry);
    }

    @Transactional(readOnly = true)
    public List<SalesAuditLogResponse> getLogsForEntity(String entityName, Long entityId) {
        return auditLogRepo.findByEntityNameAndEntityIdOrderByTimestampDesc(entityName, entityId)
                .stream()
                .map(log -> SalesAuditLogResponse.builder().id(log.getId()).entityName(log.getEntityName()).entityId(log.getEntityId()).action(log.getAction()).actor(log.getActor()).timestamp(log.getTimestamp()).payloadJson(log.getPayloadJson()).build())
                .collect(Collectors.toList());
    }
}
