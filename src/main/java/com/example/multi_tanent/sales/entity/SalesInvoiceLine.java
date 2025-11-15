package com.example.multi_tanent.sales.entity;


import com.example.multi_tanent.sales.base.AbstractLine;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "invoice_lines")
public class SalesInvoiceLine extends AbstractLine {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private SalesInvoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    private SalesOrderLine sourceOrderLine;

    @ManyToOne(fetch = FetchType.LAZY)
    private SalesDeliveryOrderLine sourceDeliveryLine;

    
}
