package com.example.multi_tanent.tenant.attendance.repository;

import com.example.multi_tanent.tenant.attendance.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    Optional<AttendanceRecord> findByEmployeeEmployeeCodeAndAttendanceDate(String employeeCode, LocalDate attendanceDate);
    List<AttendanceRecord> findByEmployeeEmployeeCodeAndAttendanceDateBetween(String employeeCode, LocalDate startDate, LocalDate endDate);
}