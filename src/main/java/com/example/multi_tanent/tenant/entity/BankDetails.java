package com.example.multi_tanent.tenant.entity;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bank_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "package_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SalaryDetails salaryDetails;

    private String accountHolderName;
    private BigInteger accountNumber;
    private String ifscOrSwift;
    private String bankName;
    private String branch;
    private Boolean payoutActive;
}
        