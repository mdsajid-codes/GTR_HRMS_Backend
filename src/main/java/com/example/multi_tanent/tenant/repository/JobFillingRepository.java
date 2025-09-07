package com.example.multi_tanent.tenant.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.multi_tanent.tenant.entity.JobFilling;

public interface JobFillingRepository extends JpaRepository<JobFilling, Long> {
    Optional<JobFilling> findByEmployeeId(Long employeeId);
}
