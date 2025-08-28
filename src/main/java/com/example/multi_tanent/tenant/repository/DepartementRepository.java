package com.example.multi_tanent.tenant.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.multi_tanent.tenant.entity.Department;

public interface DepartementRepository extends JpaRepository<Department, Long> {
     
}
