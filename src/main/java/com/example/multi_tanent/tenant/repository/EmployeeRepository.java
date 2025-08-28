// com/example/multi_tanent/tenant/repository/EmployeeRepository.java
package com.example.multi_tanent.tenant.repository;

import com.example.multi_tanent.tenant.entity.Employee;
import com.example.multi_tanent.tenant.entity.enums.Gender;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.multi_tanent.tenant.entity.enums.MartialStatus;
import com.example.multi_tanent.tenant.entity.enums.EmployeeStatus;



public interface EmployeeRepository extends JpaRepository<Employee, Long> { 
    Optional<Employee> findByUserId(Long userId);

    @EntityGraph(value = "graph.Employee.full")
    Optional<Employee> findByEmployeeCode(String employeeCode);

    @EntityGraph(value = "graph.Employee.full")
    List<Employee> findByGender(Gender gender);

    @EntityGraph(value = "graph.Employee.full")
    List<Employee> findByMartialStatus(MartialStatus martialStatus);

    @EntityGraph(value = "graph.Employee.full")
    List<Employee> findByStatus(EmployeeStatus status);

    @Override
    @EntityGraph(value = "graph.Employee.full")
    List<Employee> findAll();
}
