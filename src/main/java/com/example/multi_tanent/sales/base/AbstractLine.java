package com.example.multi_tanent.sales.base;

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

    @Column(precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 18, scale = 2)
    private BigDecimal discount;

    @Column(precision = 10, scale = 2)
    private BigDecimal taxRate;

    @Column(precision = 18, scale = 2)
    private BigDecimal lineTotal;
}
