package com.example.multi_tanent.sales.controller;

import com.example.multi_tanent.sales.dto.SalesInvoiceRequest;
import com.example.multi_tanent.sales.dto.SalesInvoiceResponse;
import com.example.multi_tanent.sales.service.SalesInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales/invoices")
@RequiredArgsConstructor
public class SalesInvoiceController {

    private final SalesInvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<SalesInvoiceResponse> createInvoice(@RequestBody SalesInvoiceRequest request) {
        return new ResponseEntity<>(invoiceService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SalesInvoiceResponse>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesInvoiceResponse> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalesInvoiceResponse> updateInvoice(@PathVariable Long id, @RequestBody SalesInvoiceRequest request) {
        return ResponseEntity.ok(invoiceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
