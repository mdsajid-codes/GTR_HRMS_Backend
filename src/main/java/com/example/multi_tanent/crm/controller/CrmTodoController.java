package com.example.multi_tanent.crm.controller;

import com.example.multi_tanent.crm.dto.*;
import com.example.multi_tanent.crm.services.CrmTodoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/crm/todos")
public class CrmTodoController {

    private final CrmTodoService service;

    @PostMapping
    public ResponseEntity<CrmTodoResponse> create(@Valid @RequestBody CrmTodoRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CrmTodoResponse> update(@PathVariable Long id, @Valid @RequestBody CrmTodoRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrmTodoResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping("/search")
    public Page<CrmTodoResponse> search(@RequestBody(required = false) CrmTodoFilterRequest filter,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        return service.search(filter, PageRequest.of(page, size));
    }

    @GetMapping("/subject-counts")
    public List<CrmTodoSubjectCount> subjectCounts() {
        return service.subjectCounts();
    }

    /* -------- labels ---------- */

    @GetMapping("/labels")
    public List<CrmTodoLabelResponse> labels() {
        return service.getLabels();
    }

    @PostMapping("/labels")
    public ResponseEntity<CrmTodoLabelResponse> createLabel(@Valid @RequestBody CrmTodoLabelRequest req) {
        return ResponseEntity.ok(service.createLabel(req));
    }

    @PutMapping("/labels/{id}")
    public ResponseEntity<CrmTodoLabelResponse> updateLabel(@PathVariable Long id,
                                                         @Valid @RequestBody CrmTodoLabelRequest req) {
        return ResponseEntity.ok(service.updateLabel(id, req));
    }

    @DeleteMapping("/labels/{id}")
    public ResponseEntity<Void> deleteLabel(@PathVariable Long id) {
        service.deleteLabel(id);
        return ResponseEntity.noContent().build();
    }
}
