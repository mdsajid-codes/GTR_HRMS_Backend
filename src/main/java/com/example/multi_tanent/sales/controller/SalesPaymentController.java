package com.example.multi_tanent.sales.controller;

import com.example.multi_tanent.sales.dto.SalesPaymentRequest;
import com.example.multi_tanent.sales.dto.SalesPaymentResponse;
import com.example.multi_tanent.sales.service.SalesPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales/payments")
@RequiredArgsConstructor
public class SalesPaymentController {

    private final SalesPaymentService paymentService;

    @PostMapping
    public ResponseEntity<SalesPaymentResponse> createPayment(@RequestBody SalesPaymentRequest request) {
        return new ResponseEntity<>(paymentService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SalesPaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesPaymentResponse> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalesPaymentResponse> updatePayment(@PathVariable Long id, @RequestBody SalesPaymentRequest request) {
        return ResponseEntity.ok(paymentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
