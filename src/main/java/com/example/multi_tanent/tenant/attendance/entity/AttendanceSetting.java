package com.example.multi_tanent.tenant.attendance.entity;

import com.example.multi_tanent.tenant.attendance.enums.AttendanceMethod;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attendance_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AttendanceMethod method; // MANUAL, BIOMETRIC, RFID, GPS, APP

    private Integer defaultGraceMinutes;

    private Boolean autoMarkAbsentAfter;

    private Integer absentAfterMinutes; // e.g., if not checked-in within this minutes, mark absent

}
