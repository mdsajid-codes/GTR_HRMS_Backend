package com.example.multi_tanent.tenant.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.multi_tanent.tenant.entity.SalaryDetails;

public interface SalaryDetailsRepository extends JpaRepository<SalaryDetails, Long> {
    Optional<SalaryDetails> findByEmployeeId(Long employeeId);
}
