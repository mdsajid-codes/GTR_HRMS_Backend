package com.example.multi_tanent.tenant.payroll.entity;

import com.example.multi_tanent.tenant.payroll.enums.PayrollStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_runs")
@Data
public class PayrollRun {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int year;
    private int month;

    private LocalDate payPeriodStart;
    private LocalDate payPeriodEnd;

    @Enumerated(EnumType.STRING)
    private PayrollStatus status; // DRAFT, PROCESSING, COMPLETED, PAID

    private LocalDateTime executedAt;
    private Long executedByUserId;
}