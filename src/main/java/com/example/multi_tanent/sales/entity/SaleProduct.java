package com.example.multi_tanent.sales.entity;

import com.example.multi_tanent.production.entity.ProCategory;
import com.example.multi_tanent.production.entity.ProSubCategory;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import com.example.multi_tanent.sales.base.AbstractAuditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Setter
@Getter
@Table(name = "sales_products", indexes = {
        @Index(name = "ix_product_tenant_sku", columnList = "tenant_id, sku", unique = true)
})
public class SaleProduct extends AbstractAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 64)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id")
    private ProSubCategory subCategory;
}
