package com.example.multi_tanent.tenant.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.multi_tanent.tenant.entity.CompensationComponents;

public interface CompensationRepository extends JpaRepository<CompensationComponents, Long> {
    
}
