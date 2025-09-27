package com.example.multi_tanent.pos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "billing_invoices")
public class BillingInvoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    private String invoiceNo;

    private Long amountCents;

    private boolean paid = false;

    private OffsetDateTime issuedAt;

    private OffsetDateTime dueAt;

    private OffsetDateTime paidAt;

    private String providerInvoiceId; // e.g., Stripe invoice id

    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (issuedAt == null) issuedAt = OffsetDateTime.now();
    }
}