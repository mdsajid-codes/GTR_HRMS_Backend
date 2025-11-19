package com.example.multi_tanent.sales.dto;

import com.example.multi_tanent.sales.entity.SalesAttachment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SalesAttachmentResponse {
    private Long id;
    private String filename;
    private String url;
    private String fileType;
    private Long fileSize;
    private LocalDateTime createdAt;

    public static SalesAttachmentResponse fromEntity(SalesAttachment entity) {
        return SalesAttachmentResponse.builder()
                .id(entity.getId())
                .filename(entity.getFilename())
                .url(entity.getUrl())
                .fileType(entity.getFileType())
                .fileSize(entity.getFileSize())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}