package com.example.multi_tanent.tenant.payroll.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "benefit_payout_files")
@Data
public class BenefitPayoutFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provision_id", nullable = false)
    @JsonBackReference
    private EmployeeBenefitProvision provision;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String originalFilename;
}