package com.example.multi_tanent.tenant.leave.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.multi_tanent.spersusers.enitity.Employee;
import com.example.multi_tanent.tenant.leave.enums.EncashmentStatus;

@Entity
@Table(name = "leave_encashment_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveEncashmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    private BigDecimal daysRequested;

    private BigDecimal amountCalculated; // rupees etc. — store computed amount

    @Enumerated(EnumType.STRING)
    private EncashmentStatus status = EncashmentStatus.DRAFT;

    private LocalDateTime requestedAt;
    private Long requestedByUserId;
    private LocalDateTime processedAt;
    private Long processedByUserId;
    
}
