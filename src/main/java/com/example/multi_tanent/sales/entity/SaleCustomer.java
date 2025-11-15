package com.example.multi_tanent.sales.entity;

import com.example.multi_tanent.sales.base.AbstractAuditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "sales_customers", indexes = {
        @Index(name = "ix_customer_code", columnList = "code", unique = true)
})
public class SaleCustomer extends AbstractAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 150)
    private String email;

    @Column(length = 30)
    private String phone;

    @Embedded
    private SalesAddress billingAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "line1", column = @Column(name = "ship_line1")),
            @AttributeOverride(name = "line2", column = @Column(name = "ship_line2")),
            @AttributeOverride(name = "city", column = @Column(name = "ship_city")),
            @AttributeOverride(name = "state", column = @Column(name = "ship_state")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "ship_postal_code")),
            @AttributeOverride(name = "country", column = @Column(name = "ship_country"))
    })
    private SalesAddress shippingAddress;

    @Column(length = 40)
    private String gstOrVatNumber;

    @Column(length = 20)
    private String status;

}
