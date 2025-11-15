package com.example.multi_tanent.sales.controller;

import com.example.multi_tanent.sales.dto.SalesDeliveryOrderLineRequest;
import com.example.multi_tanent.sales.dto.SalesDeliveryOrderLineResponse;
import com.example.multi_tanent.sales.service.SalesDeliveryOrderLineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales/delivery-orders")
@RequiredArgsConstructor
public class SalesDeliveryOrderLineController {

    private final SalesDeliveryOrderLineService doLineService;

    @PostMapping("/{deliveryOrderId}/lines")
    public ResponseEntity<SalesDeliveryOrderLineResponse> addLineToDeliveryOrder(@PathVariable Long deliveryOrderId, @RequestBody SalesDeliveryOrderLineRequest request) {
        return new ResponseEntity<>(doLineService.addLine(deliveryOrderId, request), HttpStatus.CREATED);
    }

    @GetMapping("/{deliveryOrderId}/lines")
    public ResponseEntity<List<SalesDeliveryOrderLineResponse>> getLinesForDeliveryOrder(@PathVariable Long deliveryOrderId) {
        return ResponseEntity.ok(doLineService.getAllLinesForDeliveryOrder(deliveryOrderId));
    }

    @DeleteMapping("/lines/{lineId}")
    public ResponseEntity<Void> deleteDeliveryOrderLine(@PathVariable Long lineId) {
        doLineService.deleteLine(lineId);
        return ResponseEntity.noContent().build();
    }
}
