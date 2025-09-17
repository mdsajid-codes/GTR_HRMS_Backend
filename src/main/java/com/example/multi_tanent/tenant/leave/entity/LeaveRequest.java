package com.example.multi_tanent.tenant.leave.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.multi_tanent.tenant.employee.entity.Employee;
import com.example.multi_tanent.tenant.leave.enums.LeaveStatus;

@Entity
@Table(name = "leave_requests",
       indexes = {@Index(name = "idx_leave_request_employee", columnList = "employee_id")})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // who applied
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // type of leave
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    // date range (support multi-day requests)
    private LocalDate fromDate;

    private LocalDate toDate;

    // number of days requested (can be computed but store for convenience)
    private BigDecimal daysRequested;

    // if partial days supported (morning/afternoon)
    private String partialDayInfo; // e.g., "2025-08-01:HALF_MORNING"

    private String reason;

    // status workflow
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status;

    // who currently is the approver (user id) - optional
    private Long currentApproverUserId;

    // total approved days (could be less than requested if partial approval)
    private BigDecimal daysApproved;

    // references for auditing / payroll
    private Long createdByUserId;
    private LocalDateTime createdAt;
    private Long updatedByUserId;
    private LocalDateTime updatedAt;
    private String adminNotes;
}
