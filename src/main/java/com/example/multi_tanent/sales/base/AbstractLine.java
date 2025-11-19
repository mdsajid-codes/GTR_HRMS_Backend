package com.example.multi_tanent.sales.base;

import com.example.multi_tanent.production.entity.ProTax;
import com.example.multi_tanent.production.entity.ProUnit;
import com.example.multi_tanent.sales.entity.SaleProduct;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@MappedSuperclass
@Getter
@Setter
public abstract class AbstractLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private SaleProduct product;

    @Lob
    private String description;

    @Column(precision = 18, scale = 4, nullable = false)
    private BigDecimal quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private ProUnit unit;

    @Column(precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 18, scale = 2)
    private BigDecimal discount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_id")
    private ProTax tax;

    @Column(precision = 18, scale = 2)
    private BigDecimal lineTotal;
}
