package com.example.multi_tanent.sales.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "sale_price_list_items")
@Getter
@Setter
public class SalePriceListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "price_list_id", nullable = false)
    @JsonBackReference
    private SalePriceList priceList;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private SaleProduct product;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    private LocalDate validFrom;

    private LocalDate validTo;
}