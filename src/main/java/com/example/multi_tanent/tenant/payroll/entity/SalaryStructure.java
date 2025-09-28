package com.example.multi_tanent.tenant.payroll.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

import com.example.multi_tanent.spersusers.enitity.Employee;

@Entity
@Table(name = "salary_structures")
@Data
public class SalaryStructure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @Column(nullable = false, unique = true)
    private String structureName;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @OneToMany(mappedBy = "salaryStructure", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalaryStructureComponent> components;
}