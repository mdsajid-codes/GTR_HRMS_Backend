package com.example.multi_tanent.sales.controller;

import com.example.multi_tanent.sales.dto.SalePriceListRequest;
import com.example.multi_tanent.sales.dto.SalePriceListResponse;
import com.example.multi_tanent.sales.service.SalePriceListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales/pricelists")
@RequiredArgsConstructor
public class SalePriceListController {

    private final SalePriceListService priceListService;

    @PostMapping
    public ResponseEntity<SalePriceListResponse> createPriceList(@RequestBody SalePriceListRequest request) {
        return new ResponseEntity<>(priceListService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SalePriceListResponse>> getAllPriceLists() {
        return ResponseEntity.ok(priceListService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalePriceListResponse> getPriceListById(@PathVariable Long id) {
        return ResponseEntity.ok(priceListService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalePriceListResponse> updatePriceList(@PathVariable Long id, @RequestBody SalePriceListRequest request) {
        return ResponseEntity.ok(priceListService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePriceList(@PathVariable Long id) {
        priceListService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
