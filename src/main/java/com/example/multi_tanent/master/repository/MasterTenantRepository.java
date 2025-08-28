package com.example.multi_tanent.master.repository;

import com.example.multi_tanent.master.entity.MasterTenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MasterTenantRepository extends JpaRepository<MasterTenant, Long> {
  Optional<MasterTenant> findByTenantId(String tenantId);
}
