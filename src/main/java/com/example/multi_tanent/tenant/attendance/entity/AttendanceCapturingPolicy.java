package com.example.multi_tanent.tenant.attendance.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attendance_capturing_policies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCapturingPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String policyName;

    private Integer graceTimeMinutes;

    private Integer halfDayThresholdMinutes; // if total working minutes < threshold -> half day

    private Boolean allowMultiplePunches;

    private String lateMarkRules; // description or JSON for complex rules
}
