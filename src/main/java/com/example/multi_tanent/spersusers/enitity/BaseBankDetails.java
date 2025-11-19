package com.example.multi_tanent.spersusers.enitity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "base_bank_details")
@Getter
@Setter
public class BaseBankDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private BaseCustomer party;

    // Fields
    private String bankName;
    private String accountNumber;
    private String ifsCode;
    private String ibanCode;
    private String corporateId;
    private String locationBranch;

    @Column(columnDefinition = "TEXT")
    private String branchAddress;

    private String beneficiaryMailId;
}
