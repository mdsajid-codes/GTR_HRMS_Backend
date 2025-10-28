package com.example.multi_tanent.crm.repository;



import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


import com.example.multi_tanent.crm.entity.CrmIndustry;


public interface CrmIndustryRepository extends JpaRepository<CrmIndustry, Long> {
  List<CrmIndustry> findByTenantIdOrderByNameAsc(Long tenantId);
  Optional<CrmIndustry> findByIdAndTenantId(Long id, Long tenantId);
  boolean existsByTenantIdAndNameIgnoreCase(Long tenantId, String name);
}
