package com.example.multi_tanent.tenant.employee.repository;

import com.example.multi_tanent.tenant.employee.entity.TimeAttendence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TimeAttendenceRepository extends JpaRepository<TimeAttendence, Long> {
    Optional<TimeAttendence> findByEmployeeId(Long employeeId);
    Optional<TimeAttendence> findByEmployeeEmployeeCode(String employeeCode);
}