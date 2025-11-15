package com.example.multi_tanent.sales.entity;


import com.example.multi_tanent.sales.enums.DocumentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "sales_attachments")
public class SalesAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DocumentType docType;

    private Long docId;

    private String filename;

    private String url;

   
}
