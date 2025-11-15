package com.example.multi_tanent.sales.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import com.example.multi_tanent.sales.base.AbstractDocument;

@Entity
@Setter@Getter
@Table(name = "delivery_orders")
public class SalesDeliveryOrder extends AbstractDocument {

    @ManyToOne(fetch = FetchType.LAZY)
    private SalesOrder sourceSalesOrder;

    @OneToMany(mappedBy = "deliveryOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesDeliveryOrderLine> lines = new ArrayList<>();

    @OneToMany(mappedBy = "sourceDeliveryOrder")
    private List<SalesInvoice> invoices = new ArrayList<>();

    
}
