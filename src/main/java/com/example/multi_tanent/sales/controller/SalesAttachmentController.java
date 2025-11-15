package com.example.multi_tanent.sales.controller;

import com.example.multi_tanent.sales.dto.SalesAttachmentRequest;
import com.example.multi_tanent.sales.dto.SalesAttachmentResponse;
import com.example.multi_tanent.sales.enums.DocumentType;
import com.example.multi_tanent.sales.service.SalesAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales/attachments")
@RequiredArgsConstructor
public class SalesAttachmentController {

    private final SalesAttachmentService attachmentService;

    @PostMapping
    public ResponseEntity<SalesAttachmentResponse> createAttachment(@RequestBody SalesAttachmentRequest request) {
        return new ResponseEntity<>(attachmentService.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/{docType}/{docId}")
    public ResponseEntity<List<SalesAttachmentResponse>> getAttachments(@PathVariable DocumentType docType, @PathVariable Long docId) {
        return ResponseEntity.ok(attachmentService.getAttachmentsForDocument(docType, docId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id) {
        attachmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
