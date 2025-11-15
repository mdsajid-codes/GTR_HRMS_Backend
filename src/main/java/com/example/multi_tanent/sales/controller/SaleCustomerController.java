package com.example.multi_tanent.sales.controller;

import com.example.multi_tanent.sales.dto.SaleCustomerRequest;
import com.example.multi_tanent.sales.dto.SaleCustomerResponse;
import com.example.multi_tanent.sales.service.SaleCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/sales/customers")
@RequiredArgsConstructor
public class SaleCustomerController {

    private final SaleCustomerService customerService;

    @PostMapping
    public ResponseEntity<SaleCustomerResponse> createCustomer(@RequestBody SaleCustomerRequest request) {
        SaleCustomerResponse response = customerService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SaleCustomerResponse>> getAllCustomers() {
        List<SaleCustomerResponse> responses = customerService.getAll();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleCustomerResponse> getCustomerById(@PathVariable Long id) {
        SaleCustomerResponse response = customerService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaleCustomerResponse> updateCustomer(@PathVariable Long id, @RequestBody SaleCustomerRequest request) {
        SaleCustomerResponse response = customerService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
