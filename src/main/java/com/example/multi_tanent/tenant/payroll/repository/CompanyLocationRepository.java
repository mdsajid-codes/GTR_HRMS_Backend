package com.example.multi_tanent.tenant.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.multi_tanent.tenant.base.entity.CompanyLocation;

public interface CompanyLocationRepository extends JpaRepository<CompanyLocation, Long> {
}