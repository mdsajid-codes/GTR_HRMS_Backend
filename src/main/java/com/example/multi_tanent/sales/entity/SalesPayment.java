package com.example.multi_tanent.sales.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.multi_tanent.sales.entity.SaleCustomer;
import com.example.multi_tanent.sales.enums.PaymentMethod;

@Entity
@Getter@Setter
@Table(name = "sales_payments")
public class SalesPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String number;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Column(length = 128)
    private String reference;

    @Column(precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(length = 20)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private SaleCustomer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    private SalesInvoice invoice;

   
}
