package com.example.multi_tanent.sales.controller;

import com.example.multi_tanent.sales.dto.SalesQuotationLineRequest;
import com.example.multi_tanent.sales.dto.SalesQuotationLineResponse;
import com.example.multi_tanent.sales.service.SalesQuotationLineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales/quotations")
@RequiredArgsConstructor
public class SalesQuotationLineController {

    private final SalesQuotationLineService lineService;

    @PostMapping("/{quotationId}/lines")
    public ResponseEntity<SalesQuotationLineResponse> addLineToQuotation(@PathVariable Long quotationId, @RequestBody SalesQuotationLineRequest request) {
        return new ResponseEntity<>(lineService.addLine(quotationId, request), HttpStatus.CREATED);
    }

    @GetMapping("/{quotationId}/lines")
    public ResponseEntity<List<SalesQuotationLineResponse>> getLinesForQuotation(@PathVariable Long quotationId) {
        return ResponseEntity.ok(lineService.getAllLinesForQuotation(quotationId));
    }

    @PutMapping("/lines/{lineId}")
    public ResponseEntity<SalesQuotationLineResponse> updateQuotationLine(@PathVariable Long lineId, @RequestBody SalesQuotationLineRequest request) {
        return ResponseEntity.ok(lineService.updateLine(lineId, request));
    }

    @DeleteMapping("/lines/{lineId}")
    public ResponseEntity<Void> deleteQuotationLine(@PathVariable Long lineId) {
        lineService.deleteLine(lineId);
        return ResponseEntity.noContent().build();
    }
}
