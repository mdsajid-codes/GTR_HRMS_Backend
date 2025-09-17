package com.example.multi_tanent.tenant.payroll.repository;

import com.example.multi_tanent.tenant.payroll.entity.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, Long> {
    Optional<SalaryStructure> findByEmployeeEmployeeCode(String employeeCode);
    Optional<SalaryStructure> findByEmployeeId(Long employeeId);
}