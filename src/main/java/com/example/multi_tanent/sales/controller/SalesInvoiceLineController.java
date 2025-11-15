package com.example.multi_tanent.sales.controller;

import com.example.multi_tanent.sales.dto.SalesInvoiceLineRequest;
import com.example.multi_tanent.sales.dto.SalesInvoiceLineResponse;
import com.example.multi_tanent.sales.service.SalesInvoiceLineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales/invoices")
@RequiredArgsConstructor
public class SalesInvoiceLineController {

    private final SalesInvoiceLineService lineService;

    @PostMapping("/{invoiceId}/lines")
    public ResponseEntity<SalesInvoiceLineResponse> addLineToInvoice(@PathVariable Long invoiceId, @RequestBody SalesInvoiceLineRequest request) {
        return new ResponseEntity<>(lineService.addLine(invoiceId, request), HttpStatus.CREATED);
    }

    @GetMapping("/{invoiceId}/lines")
    public ResponseEntity<List<SalesInvoiceLineResponse>> getLinesForInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(lineService.getAllLinesForInvoice(invoiceId));
    }

    @PutMapping("/lines/{lineId}")
    public ResponseEntity<SalesInvoiceLineResponse> updateInvoiceLine(@PathVariable Long lineId, @RequestBody SalesInvoiceLineRequest request) {
        return ResponseEntity.ok(lineService.updateLine(lineId, request));
    }

    @DeleteMapping("/lines/{lineId}")
    public ResponseEntity<Void> deleteInvoiceLine(@PathVariable Long lineId) {
        lineService.deleteLine(lineId);
        return ResponseEntity.noContent().build();
    }
}
