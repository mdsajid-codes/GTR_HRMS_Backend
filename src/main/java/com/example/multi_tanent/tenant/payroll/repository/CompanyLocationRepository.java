package com.example.multi_tanent.tenant.payroll.repository;

import com.example.multi_tanent.tenant.payroll.entity.CompanyLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyLocationRepository extends JpaRepository<CompanyLocation, Long> {
}