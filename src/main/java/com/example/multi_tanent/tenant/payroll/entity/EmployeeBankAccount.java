package com.example.multi_tanent.tenant.payroll.entity;

import com.example.multi_tanent.tenant.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "employee_bank_accounts")
@Data
public class EmployeeBankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String ifscCode;

    private String accountHolderName;

    private boolean isPrimary;
}