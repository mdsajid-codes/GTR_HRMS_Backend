package com.example.multi_tanent.crm.controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.multi_tanent.crm.dto.CrmProductDto;
import com.example.multi_tanent.crm.dto.CrmProductResponse;
import com.example.multi_tanent.crm.services.CrmProductService;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/crm/products")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CrmProductController {

  private final CrmProductService productService;

  // Optional filter by industry: /api/settings/products?industryId=123
  @GetMapping
  public ResponseEntity<List<CrmProductResponse>> list(@RequestParam(required = false) Long industryId) {
    return ResponseEntity.ok(productService.list(industryId));
  }

  @GetMapping("/{id}")
  public ResponseEntity<CrmProductResponse> getById(@PathVariable Long id) {
    return ResponseEntity.ok(productService.getById(id));
  }

  @PostMapping
  public ResponseEntity<CrmProductResponse> create(@Valid @RequestBody CrmProductDto req) {
    return ResponseEntity.ok(productService.create(req));
  }

  @PutMapping("/{id}")
  public ResponseEntity<CrmProductResponse> update(@PathVariable Long id,
                                        @Valid @RequestBody CrmProductDto req) {
    return ResponseEntity.ok(productService.update(id, req));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    productService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
