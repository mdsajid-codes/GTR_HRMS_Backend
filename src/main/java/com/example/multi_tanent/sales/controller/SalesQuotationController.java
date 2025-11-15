package com.example.multi_tanent.sales.controller;

import com.example.multi_tanent.sales.dto.SalesQuotationRequest;
import com.example.multi_tanent.sales.dto.SalesQuotationResponse;
import com.example.multi_tanent.sales.service.SalesQuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales/quotations")
@RequiredArgsConstructor
public class SalesQuotationController {

    private final SalesQuotationService quotationService;

    @PostMapping
    public ResponseEntity<SalesQuotationResponse> createQuotation(@RequestBody SalesQuotationRequest request) {
        return new ResponseEntity<>(quotationService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SalesQuotationResponse>> getAllQuotations() {
        return ResponseEntity.ok(quotationService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesQuotationResponse> getQuotationById(@PathVariable Long id) {
        return ResponseEntity.ok(quotationService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalesQuotationResponse> updateQuotation(@PathVariable Long id, @RequestBody SalesQuotationRequest request) {
        return ResponseEntity.ok(quotationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuotation(@PathVariable Long id) {
        quotationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}