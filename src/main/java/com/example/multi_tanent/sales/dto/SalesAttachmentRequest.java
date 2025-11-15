package com.example.multi_tanent.sales.dto;

import com.example.multi_tanent.sales.enums.DocumentType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SalesAttachmentRequest {
    private DocumentType docType;
    private Long docId;
    private String filename;
    private String url;
}
