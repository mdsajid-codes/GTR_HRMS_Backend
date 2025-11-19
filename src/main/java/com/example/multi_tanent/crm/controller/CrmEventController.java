package com.example.multi_tanent.crm.controller;
import com.example.multi_tanent.crm.dto.CrmEventRequest;
import com.example.multi_tanent.crm.dto.CrmEventResponse;
import com.example.multi_tanent.crm.services.CrmEventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crm/events")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CrmEventController {

    private final CrmEventService service;

    @GetMapping
    public ResponseEntity<List<CrmEventResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrmEventResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/lead/{leadId}")
    public ResponseEntity<List<CrmEventResponse>> getEventsByLeadId(@PathVariable Long leadId) {
        return ResponseEntity.ok(service.getEventsByLeadId(leadId));
    }

    @PostMapping
    public ResponseEntity<CrmEventResponse> create(@Valid @RequestBody CrmEventRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CrmEventResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody CrmEventRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Simple availability check: true if BUSY, false if FREE */
    @PostMapping("/availability/{employeeId}")
    public ResponseEntity<Boolean> employeeBusy(@PathVariable Long employeeId,
                                                @Valid @RequestBody CrmEventRequest req) {
        return ResponseEntity.ok(service.isEmployeeBusy(employeeId, req));
    }
}
