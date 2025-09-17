// com/example/multi_tanent/tenant/repository/EmployeeRepository.java
package com.example.multi_tanent.tenant.employee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.multi_tanent.tenant.employee.entity.Employee;
import com.example.multi_tanent.tenant.employee.enums.EmployeeStatus;
import com.example.multi_tanent.tenant.employee.enums.Gender;
import com.example.multi_tanent.tenant.employee.enums.MartialStatus;



public interface EmployeeRepository extends JpaRepository<Employee, Long> { 
    Optional<Employee> findByUserId(Long userId);

    Optional<Employee> findByEmployeeCode(String employeeCode);


    List<Employee> findByGender(Gender gender);

    List<Employee> findByMartialStatus(MartialStatus martialStatus);

    List<Employee> findByStatus(EmployeeStatus status);

    @Override
    List<Employee> findAll();
}
