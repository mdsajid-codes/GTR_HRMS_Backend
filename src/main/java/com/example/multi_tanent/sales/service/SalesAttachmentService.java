package com.example.multi_tanent.sales.service;

import com.example.multi_tanent.sales.dto.SalesAttachmentRequest;
import com.example.multi_tanent.sales.dto.SalesAttachmentResponse;
import com.example.multi_tanent.sales.entity.SalesAttachment;
import com.example.multi_tanent.sales.enums.DocumentType;
import com.example.multi_tanent.sales.repository.SalesAttachmentRepository;
import com.example.multi_tanent.sales.repository.SalesInvoiceRepository;
import com.example.multi_tanent.sales.repository.SalesOrderRepository;
import com.example.multi_tanent.sales.repository.SalesQuotationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesAttachmentService {

    private final SalesAttachmentRepository attachmentRepo;
    private final SalesQuotationRepository quotationRepo;
    private final SalesOrderRepository orderRepo;
    private final SalesInvoiceRepository invoiceRepo;

    public SalesAttachmentResponse create(SalesAttachmentRequest req) {
        validateDocumentExists(req.getDocType(), req.getDocId());

        SalesAttachment attachment = new SalesAttachment();
        attachment.setDocType(req.getDocType());
        attachment.setDocId(req.getDocId());
        attachment.setFilename(req.getFilename());
        attachment.setUrl(req.getUrl());

        return toResponse(attachmentRepo.save(attachment));
    }

    @Transactional(readOnly = true)
    public List<SalesAttachmentResponse> getAttachmentsForDocument(DocumentType docType, Long docId) {
        validateDocumentExists(docType, docId);
        return attachmentRepo.findByDocTypeAndDocId(docType, docId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void delete(Long attachmentId) {
        SalesAttachment attachment = attachmentRepo.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found: " + attachmentId));
        // Optional: Add a tenant check here if you store tenantId on the attachment itself.
        attachmentRepo.delete(attachment);
    }

    private void validateDocumentExists(DocumentType docType, Long docId) {
        boolean exists = switch (docType) {
            case QUOTATION -> quotationRepo.existsById(docId);
            case SALES_ORDER -> orderRepo.existsById(docId);
            case INVOICE -> invoiceRepo.existsById(docId);
            default -> throw new IllegalArgumentException("Unsupported document type for attachments: " + docType);
        };
        if (!exists) {
            throw new EntityNotFoundException(docType + " not found with ID: " + docId);
        }
    }

    private SalesAttachmentResponse toResponse(SalesAttachment e) {
        return SalesAttachmentResponse.builder()
                .id(e.getId())
                .docType(e.getDocType())
                .docId(e.getDocId())
                .filename(e.getFilename())
                .url(e.getUrl())
                .build();
    }
}
