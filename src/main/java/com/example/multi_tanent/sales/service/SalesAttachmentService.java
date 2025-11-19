package com.example.multi_tanent.sales.service;

import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.sales.dto.SalesAttachmentResponse;
import com.example.multi_tanent.sales.entity.SalesAttachment;
import com.example.multi_tanent.sales.enums.DocumentType;
import com.example.multi_tanent.sales.repository.SalesAttachmentRepository;
import com.example.multi_tanent.tenant.service.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesAttachmentService {

    private final SalesAttachmentRepository attachmentRepo;
    private final FileStorageService fileStorageService;

    public SalesAttachmentResponse attachFile(DocumentType docType, Long docId, MultipartFile file) throws IOException {
        String tenantId = TenantContext.getTenantId();
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String storedFilename = UUID.randomUUID().toString() + "." + extension;

        String subDirectory = "sales/" + docType.name().toLowerCase() + "/" + docId;

        String relativePath = fileStorageService.storeFile(file.getBytes(), subDirectory, storedFilename);
        String fullUrl = buildFileUrl(tenantId, relativePath);

        SalesAttachment attachment = new SalesAttachment();
        attachment.setDocType(docType);
        attachment.setDocId(docId);
        attachment.setFilename(originalFilename);
        attachment.setUrl(fullUrl);
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());

        return SalesAttachmentResponse.fromEntity(attachmentRepo.save(attachment));
    }

    @Transactional(readOnly = true)
    public List<SalesAttachmentResponse> getAttachmentsForDocument(DocumentType docType, Long docId) {
        return attachmentRepo.findByDocTypeAndDocId(docType, docId).stream()
                .map(SalesAttachmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public void deleteAttachment(DocumentType docType, Long docId, Long attachmentId) {
        SalesAttachment attachment = attachmentRepo.findByIdAndDocTypeAndDocId(attachmentId, docType, docId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found with id: " + attachmentId));

        // The URL contains the full path needed for deletion
        fileStorageService.deleteFile(attachment.getUrl());
        attachmentRepo.delete(attachment);
    }

    private String buildFileUrl(String tenantId, String relativePath) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(tenantId + "/")
                .path(relativePath)
                .build()
                .toUriString();
    }
}