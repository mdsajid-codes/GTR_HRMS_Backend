package com.example.multi_tanent.tenant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.example.multi_tanent.tenant.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    @Transactional
    long deleteByName(String name);
}
