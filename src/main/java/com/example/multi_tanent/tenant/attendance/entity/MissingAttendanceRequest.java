package com.example.multi_tanent.tenant.attendance.entity;

import com.example.multi_tanent.spersusers.enitity.Employee;
import com.example.multi_tanent.spersusers.enitity.User;
import com.example.multi_tanent.tenant.attendance.enums.MissingAttendanceRequestStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "missing_attendance_requests")
@Data
public class MissingAttendanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate attendanceDate;

    private LocalTime requestedCheckIn;
    private LocalTime requestedCheckOut;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissingAttendanceRequestStatus status;

    @CreationTimestamp
    private LocalDateTime requestDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    private LocalDateTime approvalDate;

    @Column(length = 500)
    private String approverRemarks;

    private String attachmentPath; // Path to the uploaded supporting document
}
