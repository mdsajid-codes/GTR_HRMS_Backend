package com.example.multi_tanent.tenant.payroll.repository;

import com.example.multi_tanent.tenant.payroll.entity.SalaryStructureComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalaryStructureComponentRepository extends JpaRepository<SalaryStructureComponent, Long> {
    List<SalaryStructureComponent> findBySalaryStructureId(Long salaryStructureId);
}