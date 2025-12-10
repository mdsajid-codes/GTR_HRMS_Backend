package com.example.multi_tanent.tenant.payroll.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "statutory_rules")
@Data
public class StatutoryRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ruleName; // e.g., "Provident Fund", "ESI", "Professional Tax - Maharashtra"

    private String description;

    // Example for PF
    private BigDecimal employeeContributionRate; // e.g., 12.00
    private BigDecimal employerContributionRate; // e.g., 12.00
    private BigDecimal contributionCap; // e.g., 15000.00

    // Example for Professional Tax (can be a JSON string for slab-based rules)
    @Lob
    @Column(columnDefinition = "longtext")
    private String taxSlabsJson;

    private boolean isActive;
}