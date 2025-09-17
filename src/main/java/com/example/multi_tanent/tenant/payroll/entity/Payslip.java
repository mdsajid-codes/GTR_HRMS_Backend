package com.example.multi_tanent.tenant.payroll.entity;

import com.example.multi_tanent.tenant.employee.entity.Employee;
import com.example.multi_tanent.tenant.payroll.enums.PayrollStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "payslips", indexes = {
    @Index(name = "idx_payslip_employee_period", columnList = "employee_id, year, month")
})
@Data
public class Payslip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_run_id")
    private PayrollRun payrollRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private LocalDate payDate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal grossEarnings;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDeductions;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal netSalary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayrollStatus status;

    @OneToMany(mappedBy = "payslip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PayslipComponent> components;
}