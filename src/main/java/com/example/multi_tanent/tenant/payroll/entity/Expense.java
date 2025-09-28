package com.example.multi_tanent.tenant.payroll.entity;

import com.example.multi_tanent.spersusers.enitity.Employee;
import com.example.multi_tanent.tenant.payroll.enums.ExpenseStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
@Data
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate expenseDate;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseStatus status;

    private String receiptPath; // Path to the uploaded receipt file

    private LocalDateTime submittedAt;
    private LocalDateTime processedAt;
    private Long processedByUserId;
}