package com.example.multi_tanent.crm.controller;

import com.example.multi_tanent.crm.dto.LeadSourceRequest;
import com.example.multi_tanent.crm.dto.LeadSourceResponse;
import com.example.multi_tanent.crm.services.LeadSourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crm/lead-sources")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LeadSourceContoller {

    private final LeadSourceService service;

    @GetMapping
    public ResponseEntity<List<LeadSourceResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping
    public ResponseEntity<LeadSourceResponse> create(@Valid @RequestBody LeadSourceRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeadSourceResponse> update(@PathVariable Long id, @Valid @RequestBody LeadSourceRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
