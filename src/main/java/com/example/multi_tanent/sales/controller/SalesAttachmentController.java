package com.example.multi_tanent.sales.controller;

import com.example.multi_tanent.sales.dto.SalesAttachmentResponse;
import com.example.multi_tanent.sales.enums.DocumentType;
import com.example.multi_tanent.sales.service.SalesAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/sales/attachments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SalesAttachmentController {

    private final SalesAttachmentService attachmentService;

    @PostMapping
    public ResponseEntity<SalesAttachmentResponse> uploadAttachment(
            @RequestParam("docType") DocumentType docType,
            @RequestParam("docId") Long docId,
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        SalesAttachmentResponse response = attachmentService.attachFile(docType, docId, file);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SalesAttachmentResponse>> getAttachments(
            @RequestParam("docType") DocumentType docType,
            @RequestParam("docId") Long docId) {
        return ResponseEntity.ok(attachmentService.getAttachmentsForDocument(docType, docId));
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @RequestParam("docType") DocumentType docType,
            @RequestParam("docId") Long docId,
            @PathVariable Long attachmentId) {
        attachmentService.deleteAttachment(docType, docId, attachmentId);
        return ResponseEntity.noContent().build();
    }
}