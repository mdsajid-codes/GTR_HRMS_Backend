package com.example.multi_tanent.tenant.leave.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.example.multi_tanent.tenant.leave.enums.ApprovalAction;

@Entity
@Table(name = "leave_approvals",
       indexes = {@Index(name = "idx_leave_approval_request", columnList = "leave_request_id")})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // which leave request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_request_id", nullable = false)
    private LeaveRequest leaveRequest;

    // approver (user id)
    private Long approverUserId;

    // approval action & comment
    @Enumerated(EnumType.STRING)
    private ApprovalAction action; // APPROVED / REJECTED / ESCALATED

    @Column(length = 1000)
    private String comment;

    private LocalDateTime actionAt;

}
