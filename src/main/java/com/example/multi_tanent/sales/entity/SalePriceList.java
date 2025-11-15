package com.example.multi_tanent.sales.entity;

import com.example.multi_tanent.sales.base.AbstractAuditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;



@Entity
@Table(name = "sale_price_lists")
@Getter
@Setter
public class SalePriceList extends AbstractAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String currency;

    @Column(name = "is_default")
    private boolean isDefault;
}
