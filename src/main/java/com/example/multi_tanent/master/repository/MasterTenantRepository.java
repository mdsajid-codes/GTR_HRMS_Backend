package com.example.multi_tanent.master.repository;

import com.example.multi_tanent.master.entity.MasterTenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MasterTenantRepository extends JpaRepository<MasterTenant, Long> {
  Optional<MasterTenant> findByTenantId(String tenantId);
   @Query("SELECT m.tenantId FROM MasterTenant m")
    List<String> findAllTenantIds();
}
