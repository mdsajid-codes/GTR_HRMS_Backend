package com.example.multi_tanent.sales.controller;

import com.example.multi_tanent.sales.dto.SalesTermAndConditionRequest;
import com.example.multi_tanent.sales.dto.SalesTermAndConditionResponse;
import com.example.multi_tanent.sales.service.SalesTermAndConditionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales/terms-and-conditions")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SalesTermAndConditionController {

    private final SalesTermAndConditionService service;

    @PostMapping
    public ResponseEntity<SalesTermAndConditionResponse> create(@Valid @RequestBody SalesTermAndConditionRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SalesTermAndConditionResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesTermAndConditionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalesTermAndConditionResponse> update(@PathVariable Long id, @Valid @RequestBody SalesTermAndConditionRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}