package com.example.multi_tanent.crm.controller;

import com.example.multi_tanent.crm.dto.CrmSalesProductRequest;
import com.example.multi_tanent.crm.dto.CrmSalesProductResponse;
import com.example.multi_tanent.crm.services.CrmSalesProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/crm/sales-products")
@RequiredArgsConstructor
public class CrmSalesProductController {

    private final CrmSalesProductService service;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CrmSalesProductResponse> createProductJson(@RequestBody CrmSalesProductRequest request) {
        return ResponseEntity.ok(service.createProduct(request, null));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CrmSalesProductResponse> createProductMultipart(
            @RequestPart("product") CrmSalesProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(service.createProduct(request, image));
    }

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<CrmSalesProductResponse>> getAllProducts(
            @org.springframework.data.web.PageableDefault(size = 10) org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(service.getAllProducts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrmSalesProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getProductById(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CrmSalesProductResponse> updateProductJson(@PathVariable Long id,
            @RequestBody CrmSalesProductRequest request) {
        return ResponseEntity.ok(service.updateProduct(id, request, null));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CrmSalesProductResponse> updateProductMultipart(@PathVariable Long id,
            @RequestPart(value = "product", required = false) CrmSalesProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(service.updateProduct(id, request, image));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        service.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<org.springframework.core.io.Resource> getProductImageById(@PathVariable Long id,
            jakarta.servlet.http.HttpServletRequest request) {
        CrmSalesProductResponse product = service.getProductById(id);
        if (product.getImageUrl() == null || product.getImageUrl().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        org.springframework.core.io.Resource resource = service.loadFileAsResource(product.getImageUrl());

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (java.io.IOException ex) {
            // fallback
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/images/**")
    public ResponseEntity<org.springframework.core.io.Resource> getProductImage(
            jakarta.servlet.http.HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String filePath = requestUri.substring(requestUri.indexOf("/images/") + "/images/".length());

        org.springframework.core.io.Resource resource = service.loadFileAsResource(filePath);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (java.io.IOException ex) {
            // fallback
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
