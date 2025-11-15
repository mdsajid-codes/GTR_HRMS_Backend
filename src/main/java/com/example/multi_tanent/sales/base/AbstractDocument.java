package com.example.multi_tanent.sales.base;

import com.example.multi_tanent.sales.entity.SaleCustomer;
import com.example.multi_tanent.sales.enums.DocumentStatus;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@MappedSuperclass
@Setter@Getter
public abstract class AbstractDocument extends AbstractAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String number;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private SaleCustomer customer;

    @Column(length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    @Column(precision = 18, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 18, scale = 2)
    private BigDecimal taxTotal;

    @Column(precision = 18, scale = 2)
    private BigDecimal grandTotal;

    @Column(length = 1024)
    private String notes;

    @Column(length = 100)
    private String createdBy;

    
}
