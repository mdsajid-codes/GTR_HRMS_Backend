package com.example.multi_tanent.sales.entity;


import com.example.multi_tanent.sales.base.AbstractLine;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter@Getter
@Table(name = "delivery_order_lines")
public class SalesDeliveryOrderLine extends AbstractLine {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private SalesDeliveryOrder deliveryOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    private SalesOrderLine sourceOrderLine;

   
}
