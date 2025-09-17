package com.example.multi_tanent.tenant.payroll.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "company_info")
@Data
public class CompanyInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Should only be one record per tenant

    @Column(nullable = false)
    private String companyName;

    // These fields can represent the primary/registered office address.
    // Additional branch offices can be managed via the 'locations' list.
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String phone;
    private String email;
    private String website;

    // Statutory Details
    private String pan; // Permanent Account Number
    private String tan; // Tax Deduction and Collection Account Number
    private String gstIn; // Goods and Services Tax Identification Number
    private String pfRegistrationNumber;
    private String esiRegistrationNumber;

    @OneToMany(mappedBy = "companyInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompanyLocation> locations;

    @OneToMany(mappedBy = "companyInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompanyBankAccount> bankAccounts;
}