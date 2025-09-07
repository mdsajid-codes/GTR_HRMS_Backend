package com.example.multi_tanent.tenant.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.multi_tanent.tenant.entity.CompensationComponents;
import com.example.multi_tanent.tenant.entity.enums.ComponentType;

public interface CompensationRepository extends JpaRepository<CompensationComponents, Long> {
    Optional<CompensationComponents> findBySalaryDetailsIdAndComponentType(Long salaryDetailsId, ComponentType componentType);
}
