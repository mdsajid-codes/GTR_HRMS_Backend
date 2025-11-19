package com.example.multi_tanent.spersusers.enitity;

import com.example.multi_tanent.sales.entity.SaleCustomer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "other_persons")
@Getter
@Setter
public class OtherPerson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private BaseCustomer party;

    // Fields
    private String salutation;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String workPhone;
    private String mobile;
    private String skypeNameOrNumber;
    private String designation;
    private String department;
}
