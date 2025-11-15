package com.example.multi_tanent.sales.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.multi_tanent.sales.base.AbstractDocument;
import com.example.multi_tanent.sales.enums.InvoiceType;

@Entity
@Setter@Getter
@Table(name = "sales_invoices")
public class SalesInvoice extends AbstractDocument {

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private InvoiceType type = InvoiceType.TAX;

    private LocalDateTime postedAt;
    private LocalDateTime voidedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private SalesOrder sourceSalesOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    private SalesDeliveryOrder sourceDeliveryOrder;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesInvoiceLine> lines = new ArrayList<>();

    @OneToMany(mappedBy = "invoice")
    private List<SalesPayment> payments = new ArrayList<>();

   
}
