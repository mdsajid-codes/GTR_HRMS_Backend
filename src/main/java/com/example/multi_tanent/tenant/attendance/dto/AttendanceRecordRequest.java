package com.example.multi_tanent.tenant.attendance.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.multi_tanent.tenant.attendance.enums.AttendanceStatus;

@Data
public class AttendanceRecordRequest {
    private String employeeCode;
    private LocalDate attendanceDate;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private AttendanceStatus status;
    private String remarks;
    private Long shiftPolicyId; // Optional: to assign a specific shift for this day
}