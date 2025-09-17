package com.example.multi_tanent.tenant.payroll.repository;

import com.example.multi_tanent.tenant.payroll.entity.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PayslipRepository extends JpaRepository<Payslip, Long> {
    List<Payslip> findByEmployeeEmployeeCodeOrderByYearDescMonthDesc(String employeeCode);
    Optional<Payslip> findByEmployeeIdAndYearAndMonth(Long employeeId, int year, int month);
}