package com.example.multi_tanent.sales.controller;

import com.example.multi_tanent.sales.dto.SalesOrderRequest;
import com.example.multi_tanent.sales.dto.SalesOrderResponse;
import com.example.multi_tanent.sales.service.SalesOrderService;
import com.example.multi_tanent.sales.service.SalesQuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales/orders")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SalesOrderController {

    private final SalesOrderService orderService;
    private final SalesQuotationService quotationService;

    @PostMapping
    public ResponseEntity<SalesOrderResponse> createOrder(@RequestBody SalesOrderRequest request) {
        return new ResponseEntity<>(orderService.create(request), HttpStatus.CREATED);
    }

    @PostMapping("/from-quotation/{quotationId}")
    public ResponseEntity<SalesOrderResponse> createOrderFromQuotation(@PathVariable Long quotationId) {
        SalesOrderResponse response = orderService.createFromQuotation(quotationId);
        // Mark the original quotation as ordered
        quotationService.markAsOrdered(quotationId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SalesOrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesOrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalesOrderResponse> updateOrder(@PathVariable Long id, @RequestBody SalesOrderRequest request) {
        return ResponseEntity.ok(orderService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}