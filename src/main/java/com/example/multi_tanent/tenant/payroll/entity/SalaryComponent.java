package com.example.multi_tanent.tenant.payroll.entity;

import com.example.multi_tanent.tenant.payroll.enums.CalculationType;
import com.example.multi_tanent.tenant.payroll.enums.SalaryComponentType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "salary_components")
@Data
public class SalaryComponent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., "Basic", "HRA", "Provident Fund"

    @Column(nullable = false, unique = true)
    private String code; // e.g., "BASIC", "HRA", "PF"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalaryComponentType type; // EARNING, DEDUCTION

    @Enumerated(EnumType.STRING)
    private CalculationType calculationType;

    private String formula; // To be used if calculationType is FORMULA_BASED

    private boolean isTaxable;

    private boolean partOfGrossSalary;

    private Integer displayOrder;
}