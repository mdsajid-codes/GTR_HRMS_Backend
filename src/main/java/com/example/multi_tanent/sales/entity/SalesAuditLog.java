package com.example.multi_tanent.sales.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter@Getter
@Table(name = "sales_audit_logs", indexes = {
        @Index(name = "ix_audit_entity", columnList = "entityName,entityId")
})
public class SalesAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 128)
    private String entityName;

    private Long entityId;

    @Column(length = 32)
    private String action;

    @Column(length = 100)
    private String actor;

    private LocalDateTime timestamp;

    @Lob
    private String payloadJson;

    @PrePersist
    public void prePersist() {
        timestamp = LocalDateTime.now();
    }

   
}
