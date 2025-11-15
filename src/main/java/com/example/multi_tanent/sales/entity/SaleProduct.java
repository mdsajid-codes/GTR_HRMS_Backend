package com.example.multi_tanent.sales.entity;

import com.example.multi_tanent.sales.base.AbstractAuditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Setter
@Getter
@Table(name = "sales_products", indexes = {
        @Index(name = "ix_product_sku", columnList = "sku", unique = true)
})
public class SaleProduct extends AbstractAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    private String sku;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 512)
    private String description;

    @Column(length = 16)
    private String uom;

    @Column(precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(length = 20)
    private String status;

}
