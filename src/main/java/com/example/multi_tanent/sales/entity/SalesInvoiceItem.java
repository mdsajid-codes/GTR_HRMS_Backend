package com.example.multi_tanent.sales.entity;

import com.example.multi_tanent.crm.entity.CrmSalesProduct;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sales_invoice_items")
public class SalesInvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sales_invoice_id", nullable = false)
    private SalesInvoice salesInvoice;

    @ManyToOne
    @JoinColumn(name = "crm_product_id")
    private CrmSalesProduct crmProduct;

    @Column(name = "item_code")
    private String itemCode;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "description")
    private String description;

    @Column(name = "packing_type")
    private String packingType;

    // Specific Quantities
    @Column(name = "quantity_gross")
    private BigDecimal quantityGross;

    @Column(name = "quantity_net")
    private BigDecimal quantityNet;

    @Column(name = "send_quantity")
    private BigDecimal sendQuantity;

    @Column(name = "invoice_quantity")
    private BigDecimal invoiceQuantity;

    private BigDecimal rate;

    private BigDecimal amount; // Typically invoiceQuantity * rate

    @Column(name = "tax_value")
    private BigDecimal taxValue;

    @Column(name = "is_tax_exempt")
    private boolean isTaxExempt;

    @Column(name = "tax_percentage")
    private BigDecimal taxPercentage;
}
