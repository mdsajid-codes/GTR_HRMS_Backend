package com.example.multi_tanent.sales.controller;

import com.example.multi_tanent.sales.dto.QuotationRequest;
import com.example.multi_tanent.sales.dto.QuotationResponse;
import com.example.multi_tanent.sales.enums.SalesStatus;
import com.example.multi_tanent.sales.service.QuotationPdfService;
import com.example.multi_tanent.sales.service.QuotationService;
import com.example.multi_tanent.sales.dto.PdfGenerationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales/quotations")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService quotationService;
    private final QuotationPdfService quotationPdfService;

    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuotationResponse> createQuotation(
            @RequestPart("quotation") QuotationRequest request,
            @RequestPart(value = "attachments", required = false) org.springframework.web.multipart.MultipartFile[] attachments) {
        return ResponseEntity.ok(quotationService.createQuotation(request, attachments));
    }

    @PutMapping(value = "/{id}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuotationResponse> updateQuotation(@PathVariable Long id,
            @RequestPart("quotation") QuotationRequest request,
            @RequestPart(value = "attachments", required = false) org.springframework.web.multipart.MultipartFile[] attachments) {
        return ResponseEntity.ok(quotationService.updateQuotation(id, request, attachments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuotationResponse> getQuotationById(@PathVariable Long id) {
        return ResponseEntity.ok(quotationService.getQuotationById(id));
    }

    @GetMapping
    public ResponseEntity<Page<QuotationResponse>> getAllQuotations(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(quotationService.getAllQuotations(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuotation(@PathVariable Long id) {
        quotationService.deleteQuotation(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<QuotationResponse> updateStatus(@PathVariable Long id, @RequestParam SalesStatus status) {
        return ResponseEntity.ok(quotationService.updateStatus(id, status));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        QuotationResponse quotation = quotationService.getQuotationById(id);
        byte[] pdfBytes = quotationPdfService.generateQuotationPdf(quotation);

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=quotation_" + quotation.getQuotationNumber() + ".pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/generate-pdf")
    public ResponseEntity<byte[]> generatePdfFromHtml(@RequestBody PdfGenerationRequest request) {
        byte[] pdfBytes = quotationPdfService.generatePdfFromHtml(request.getHtmlContent());
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=generated_quotation.pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
