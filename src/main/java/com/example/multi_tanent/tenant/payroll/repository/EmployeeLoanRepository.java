package com.example.multi_tanent.tenant.payroll.repository;

import com.example.multi_tanent.tenant.payroll.entity.EmployeeLoan;
import com.example.multi_tanent.tenant.payroll.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeLoanRepository extends JpaRepository<EmployeeLoan, Long> {
    List<EmployeeLoan> findByEmployeeEmployeeCodeAndStatus(String employeeCode, LoanStatus status);
}