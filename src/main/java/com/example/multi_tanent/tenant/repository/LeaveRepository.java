package com.example.multi_tanent.tenant.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.multi_tanent.tenant.entity.Leave;

public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByEmployeeId(Long employeeId);
}
