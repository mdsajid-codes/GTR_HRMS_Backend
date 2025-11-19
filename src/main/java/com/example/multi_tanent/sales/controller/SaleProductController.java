package com.example.multi_tanent.sales.controller;

import com.example.multi_tanent.sales.dto.SaleProductRequest;
import com.example.multi_tanent.sales.dto.SaleProductResponse;
import com.example.multi_tanent.sales.service.SaleProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales/products")
@RequiredArgsConstructor
public class SaleProductController {

    private final SaleProductService productService;

    @PostMapping
    public ResponseEntity<SaleProductResponse> createProduct(@Valid @RequestBody SaleProductRequest request) {
        SaleProductResponse response = productService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<SaleProductResponse>> getAllProducts(Pageable pageable) {
        Page<SaleProductResponse> responses = productService.getAll(pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleProductResponse> getProductById(@PathVariable Long id) {
        SaleProductResponse response = productService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaleProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody SaleProductRequest request) {
        SaleProductResponse response = productService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
