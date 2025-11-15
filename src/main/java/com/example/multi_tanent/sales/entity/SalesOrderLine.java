package com.example.multi_tanent.sales.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

import com.example.multi_tanent.sales.base.AbstractLine;

@Entity
@Getter@Setter
@Table(name = "sales_order_lines")
public class SalesOrderLine extends AbstractLine {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private SalesOrder salesOrder;

    @Column(precision = 18, scale = 3)
    private BigDecimal deliveredQty;

    @Column(precision = 18, scale = 3)
    private BigDecimal invoicedQty;

   
}
