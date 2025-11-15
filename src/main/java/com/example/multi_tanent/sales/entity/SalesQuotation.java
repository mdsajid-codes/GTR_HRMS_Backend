package com.example.multi_tanent.sales.entity;

import com.example.multi_tanent.sales.base.AbstractDocument;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_quotations")
@Getter
@Setter

public class SalesQuotation extends AbstractDocument {

    private LocalDate expiryDate;

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesQuotationLine> items = new ArrayList<>();

    @OneToMany(mappedBy = "sourceQuotation")
    private List<SalesOrder> derivedOrders = new ArrayList<>();

   
}
