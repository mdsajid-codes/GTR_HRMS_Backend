package com.example.multi_tanent.sales.controller;

import com.example.multi_tanent.sales.dto.SalesOrderLineRequest;
import com.example.multi_tanent.sales.dto.SalesOrderLineResponse;
import com.example.multi_tanent.sales.service.SalesOrderLineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales/orders")
@RequiredArgsConstructor
public class SalesOrderLineController {

    private final SalesOrderLineService lineService;

    @PostMapping("/{orderId}/lines")
    public ResponseEntity<SalesOrderLineResponse> addLineToOrder(@PathVariable Long orderId, @RequestBody SalesOrderLineRequest request) {
        return new ResponseEntity<>(lineService.addLine(orderId, request), HttpStatus.CREATED);
    }

    @GetMapping("/{orderId}/lines")
    public ResponseEntity<List<SalesOrderLineResponse>> getLinesForOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(lineService.getAllLinesForOrder(orderId));
    }

    @PutMapping("/lines/{lineId}")
    public ResponseEntity<SalesOrderLineResponse> updateOrderLine(@PathVariable Long lineId, @RequestBody SalesOrderLineRequest request) {
        return ResponseEntity.ok(lineService.updateLine(lineId, request));
    }

    @DeleteMapping("/lines/{lineId}")
    public ResponseEntity<Void> deleteOrderLine(@PathVariable Long lineId) {
        lineService.deleteLine(lineId);
        return ResponseEntity.noContent().build();
    }
}
