package com.example.multi_tanent.sales.controller;

import com.example.multi_tanent.sales.dto.SalesDeliveryOrderRequest;
import com.example.multi_tanent.sales.dto.SalesDeliveryOrderResponse;
import com.example.multi_tanent.sales.service.SalesDeliveryOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales/delivery-orders")
@RequiredArgsConstructor
public class SalesDeliveryOrderController {

    private final SalesDeliveryOrderService doService;

    @PostMapping
    public ResponseEntity<SalesDeliveryOrderResponse> createDeliveryOrder(@RequestBody SalesDeliveryOrderRequest request) {
        return new ResponseEntity<>(doService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SalesDeliveryOrderResponse>> getAllDeliveryOrders() {
        return ResponseEntity.ok(doService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesDeliveryOrderResponse> getDeliveryOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(doService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalesDeliveryOrderResponse> updateDeliveryOrder(@PathVariable Long id, @RequestBody SalesDeliveryOrderRequest request) {
        return ResponseEntity.ok(doService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeliveryOrder(@PathVariable Long id) {
        doService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
