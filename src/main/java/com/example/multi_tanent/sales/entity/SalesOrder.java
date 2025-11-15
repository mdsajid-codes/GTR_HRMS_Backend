package com.example.multi_tanent.sales.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.multi_tanent.sales.base.AbstractDocument;

@Entity
@Getter
@Setter
@Table(name = "sales_orders")
public class SalesOrder extends AbstractDocument {

    private LocalDate orderDate;

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesOrderLine> lines = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private SalesQuotation sourceQuotation;

    @OneToMany(mappedBy = "sourceSalesOrder")
    private List<SalesDeliveryOrder> deliveryOrders = new ArrayList<>();

    @OneToMany(mappedBy = "sourceSalesOrder")
    private List<SalesInvoice> invoices = new ArrayList<>();

   
}
