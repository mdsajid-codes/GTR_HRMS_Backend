package com.example.multi_tanent.tenant.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.multi_tanent.tenant.entity.Employee;
import com.example.multi_tanent.tenant.entity.Payroll;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    Optional<Payroll> findByEmployeeAndPayPeriodStartAndPayPeriodEnd(Employee employee, LocalDate payPeriodStart,
            LocalDate payPeriodEnd);

    Optional<Payroll> findByEmployee_EmployeeCodeAndPayPeriodStart(String employeeCode, LocalDate payPeriodStart);

    @Query("SELECT p FROM Payroll p JOIN FETCH p.employee e LEFT JOIN FETCH e.jobDetails WHERE p.id = :payrollId")
    Optional<Payroll> findByIdWithEmployeeAndJobDetails(@Param("payrollId") Long payrollId);

    @Query("SELECT p FROM Payroll p JOIN FETCH p.employee e LEFT JOIN FETCH e.jobDetails WHERE e.employeeCode = :employeeCode AND YEAR(p.payPeriodStart) = :year AND MONTH(p.payPeriodStart) = :month")
    Optional<Payroll> findByEmployeeCodeAndYearAndMonthWithDetails(@Param("employeeCode") String employeeCode, @Param("year") int year, @Param("month") int month);
    
}
