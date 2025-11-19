package com.example.multi_tanent.sales.entity;

import com.example.multi_tanent.sales.base.AbstractAuditable;
import com.example.multi_tanent.sales.enums.DocumentType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "sales_attachments")
public class SalesAttachment extends AbstractAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType docType;

    @Column(nullable = false)
    private Long docId;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String url;

    private String fileType;

    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docId", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private SalesQuotation quotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docId", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docId", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private SalesInvoice salesInvoice;
}