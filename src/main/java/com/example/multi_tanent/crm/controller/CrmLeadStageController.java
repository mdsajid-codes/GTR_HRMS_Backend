package com.example.multi_tanent.crm.controller;

import com.example.multi_tanent.crm.dto.CrmLeadStageRequest;
import com.example.multi_tanent.crm.dto.CrmLeadStageResponse;
import com.example.multi_tanent.crm.services.CrmLeadStageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crm/lead-stages")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CrmLeadStageController {

    private final CrmLeadStageService service;

    @GetMapping
    public ResponseEntity<List<CrmLeadStageResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrmLeadStageResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<CrmLeadStageResponse> create(@Valid @RequestBody CrmLeadStageRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CrmLeadStageResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody CrmLeadStageRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/move-up")
    public ResponseEntity<CrmLeadStageResponse> moveUp(@PathVariable Long id) {
        return ResponseEntity.ok(service.moveUp(id));
    }

    @PostMapping("/{id}/move-down")
    public ResponseEntity<CrmLeadStageResponse> moveDown(@PathVariable Long id) {
        return ResponseEntity.ok(service.moveDown(id));
    }

    @PostMapping("/{id}/set-default")
    public ResponseEntity<CrmLeadStageResponse> setDefault(@PathVariable Long id) {
        return ResponseEntity.ok(service.setDefault(id));
    }
}