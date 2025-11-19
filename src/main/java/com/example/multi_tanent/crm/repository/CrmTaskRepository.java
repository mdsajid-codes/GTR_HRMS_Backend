package com.example.multi_tanent.crm.repository;


import com.example.multi_tanent.crm.entity.CrmTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CrmTaskRepository extends JpaRepository<CrmTask, Long> {
    List<CrmTask> findByTenantIdOrderByDueDateAscIdAsc(Long tenantId);
    Optional<CrmTask> findByIdAndTenantId(Long id, Long tenantId);
    List<CrmTask> findByTenantIdAndLeadIdOrderByDueDateAscIdAsc(Long tenantId, Long leadId);
}
