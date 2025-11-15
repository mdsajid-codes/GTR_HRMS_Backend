package com.example.multi_tanent.production.controller;

import com.example.multi_tanent.production.dto.ProRawMaterialsRequest;
import com.example.multi_tanent.production.dto.ProRawMaterialsResponse;
import com.example.multi_tanent.production.services.ProRawMaterialsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/production/raw-materials")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class ProRawMaterialsController {

    private final ProRawMaterialsService service;

    @PostMapping
    public ResponseEntity<ProRawMaterialsResponse> create(@Valid @RequestBody ProRawMaterialsRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<ProRawMaterialsResponse>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProRawMaterialsResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/by-item-code/{itemCode}")
    public ResponseEntity<ProRawMaterialsResponse> getByItemCode(@PathVariable String itemCode) {
        return ResponseEntity.ok(service.getByItemCode(itemCode));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProRawMaterialsResponse> update(@PathVariable Long id, @Valid @RequestBody ProRawMaterialsRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> bulkCreate(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload an Excel file.");
        }
        try {
            List<String> errors = service.bulkCreate(file);
            if (errors.isEmpty()) {
                return ResponseEntity.ok("Raw materials imported successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process file: " + e.getMessage());
        }
    }

    @GetMapping("/bulk-template")
    public ResponseEntity<byte[]> downloadBulkTemplate() throws java.io.IOException {
        return service.generateBulkUploadTemplate();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAll() throws java.io.IOException {
        return service.exportAll();
    }
}
