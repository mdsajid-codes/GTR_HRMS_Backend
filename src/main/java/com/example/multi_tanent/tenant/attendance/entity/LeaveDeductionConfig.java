package com.example.multi_tanent.tenant.attendance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "leave_deduction_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveDeductionConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", unique = true)
    private AttendancePolicy policy;

    // Q1: Missing Attendance
    private Boolean deductMissingAttendance = false;

    // Q2: Late Arrival
    private Boolean penalizeLateArrival = false;

    // Q3: Work Hours Shortage
    private Boolean deductForWorkHoursShortage = false;

    // Q4: Missing Swipes
    private Boolean deductForMissingSwipes = false;

    // Q5: Early Going
    private Boolean penalizeEarlyGoing = false;
}