package com.example.multi_tanent.sales.entity;


import com.example.multi_tanent.sales.base.AbstractLine;

import com.example.multi_tanent.sales.base.AbstractLine;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
@Table(name = "sales_quotation_lines")
public class SalesQuotationLine extends AbstractLine {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private SalesQuotation quotation;

}
