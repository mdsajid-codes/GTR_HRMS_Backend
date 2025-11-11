package com.example.multi_tanent.tenant.payroll.entity;

import com.example.multi_tanent.spersusers.enitity.Employee;
import com.example.multi_tanent.tenant.payroll.enums.PaymentMethod;
import com.example.multi_tanent.tenant.payroll.enums.ExpenseStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private String billNumber;
    private String merchentName;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseStatus status;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseFile> attachments = new ArrayList<>();

    private LocalDateTime submittedAt;

    // --- Payout Details ---
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private LocalDate paidOutDate;

    @Column(length = 500)
    private String paymentDetails; // e.g., Transaction ID, Cheque No.

    private LocalDateTime processedAt;
    private Long processedByUserId;

    public void addAttachment(String filePath, String originalFilename) {
        ExpenseFile attachment = new ExpenseFile();
        attachment.setFilePath(filePath);
        attachment.setOriginalFilename(originalFilename);
        attachment.setExpense(this);
        this.attachments.add(attachment);
    }
}