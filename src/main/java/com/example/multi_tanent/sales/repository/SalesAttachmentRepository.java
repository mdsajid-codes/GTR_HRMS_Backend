package com.example.multi_tanent.sales.repository;

import com.example.multi_tanent.sales.entity.SalesAttachment;
import com.example.multi_tanent.sales.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesAttachmentRepository extends JpaRepository<SalesAttachment, Long> {
    List<SalesAttachment> findByDocTypeAndDocId(DocumentType docType, Long docId);
}
