package com.example.multi_tanent.tenant.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.multi_tanent.tenant.entity.enums.PayFrequency;
import com.example.multi_tanent.tenant.entity.enums.PayrollStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "payrolls")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payroll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "employee_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Employee employee;

    private LocalDate payPeriodStart;
    private LocalDate payPeriodEnd;

    @Enumerated(EnumType.STRING)
    private PayFrequency payFrequency;

    private Double grossSalary;
    private Double netSalary;
    private Double basicSalary;
    private Double allowances;
    private Double deductions;
    private Double taxAmount;
    private String currency; 

    @Enumerated(EnumType.STRING)
    private PayrollStatus status;

    private LocalDate payoutDate;
    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
