package com.example.multi_tanent.sales.controller;

import com.example.multi_tanent.sales.dto.RentalQuotationRequest;
import com.example.multi_tanent.sales.dto.RentalQuotationResponse;
import com.example.multi_tanent.sales.enums.SalesStatus;
import com.example.multi_tanent.sales.service.RentalQuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales/rental-quotations")
@RequiredArgsConstructor
public class RentalQuotationController {

    private final RentalQuotationService rentalQuotationService;

    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RentalQuotationResponse> createRentalQuotation(
            @RequestPart("quotation") RentalQuotationRequest request,
            @RequestPart(value = "attachments", required = false) org.springframework.web.multipart.MultipartFile[] attachments) {
        return ResponseEntity.ok(rentalQuotationService.createRentalQuotation(request, attachments));
    }

    @PutMapping(value = "/{id}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RentalQuotationResponse> updateRentalQuotation(@PathVariable Long id,
            @RequestPart("quotation") RentalQuotationRequest request,
            @RequestPart(value = "attachments", required = false) org.springframework.web.multipart.MultipartFile[] attachments) {
        return ResponseEntity.ok(rentalQuotationService.updateRentalQuotation(id, request, attachments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalQuotationResponse> getRentalQuotationById(@PathVariable Long id) {
        return ResponseEntity.ok(rentalQuotationService.getRentalQuotationById(id));
    }

    @GetMapping
    public ResponseEntity<Page<RentalQuotationResponse>> getAllRentalQuotations(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(rentalQuotationService.getAllRentalQuotations(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRentalQuotation(@PathVariable Long id) {
        rentalQuotationService.deleteRentalQuotation(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RentalQuotationResponse> updateStatus(@PathVariable Long id,
            @RequestParam SalesStatus status) {
        return ResponseEntity.ok(rentalQuotationService.updateStatus(id, status));
    }
}
