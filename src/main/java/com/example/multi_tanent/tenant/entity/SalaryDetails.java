package com.example.multi_tanent.tenant.entity;

import java.util.Set;

import com.example.multi_tanent.tenant.entity.enums.PayFrequency;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "salary_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "employee_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private PayFrequency payFrequency;

    private Double ctcAnnual;
    private Boolean bonusEligible;
    private Double bonusTargetPct;
    private String currency;

    @OneToMany(mappedBy = "salaryDetails", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<CompensationComponents> compensationComponents;

    @OneToMany(mappedBy = "salaryDetails", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<BankDetails> bankDetails;
}
