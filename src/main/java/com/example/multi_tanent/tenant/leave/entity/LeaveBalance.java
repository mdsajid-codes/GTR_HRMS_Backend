package com.example.multi_tanent.tenant.leave.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.math.BigDecimal;

import com.example.multi_tanent.tenant.employee.entity.Employee;

@Entity
@Table(name = "leave_balances",
       uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "leave_type_id", "as_of_date"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // employee
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // leave type
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    private Integer year; // financial or calendar year depending on policy

    // Total allocated for this year (including carry forward applied)
    @Column(precision = 5, scale = 2)
    private BigDecimal totalAllocated = BigDecimal.ZERO;

    // Already used (approved and taken)
    @Column(precision = 5, scale = 2)
    private BigDecimal used = BigDecimal.ZERO;

    // Pending requests count towards pending but not yet deducted from used
    @Column(precision = 5, scale = 2)
    private BigDecimal pending = BigDecimal.ZERO;

    // convenience getter (not persisted) could be calculated in service
    @Transient
    public BigDecimal getAvailable() {
        return totalAllocated.subtract(used).subtract(pending);
    }

    // date this balance is valid for (useful for snapshots)
    private LocalDate asOfDate;

    // audit
    private java.time.LocalDateTime updatedAt;
    private Long updatedByUserId;
}
