// package com.example.multi_tanent.crm.repository;

// import com.example.multi_tanent.crm.entity.CrmLead;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.List;
// import java.util.Optional;

// public interface CrmLeadRepository extends JpaRepository<CrmLead, Long> {
//     Page<CrmLead> findByTenantId(Long tenantId, Pageable pageable);
//     Optional<CrmLead> findByIdAndTenantId(Long id, Long tenantId);
// }
package com.example.multi_tanent.crm.repository;

import com.example.multi_tanent.crm.entity.CrmLead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.multi_tanent.crm.enums.CrmLeadStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface CrmLeadRepository extends JpaRepository<CrmLead, Long> {

    Page<CrmLead> findByTenantId(Long tenantId, Pageable pageable);
    Optional<CrmLead> findByIdAndTenantId(Long id, Long tenantId);

    long countByTenantId(Long tenantId);
    long countByTenantIdAndCreatedAtAfter(Long tenantId, OffsetDateTime date);
    long countByTenantIdAndOwnerIsNull(Long tenantId);
    long countByTenantIdAndStatus(Long tenantId, CrmLeadStatus status);
    long countByTenantIdAndStatusIn(Long tenantId, List<CrmLeadStatus> statuses);

    @Query("SELECT count(DISTINCT l.id) FROM CrmLead l JOIN l.tasks t WHERE l.tenant.id = :tenantId AND t.status <> 'COMPLETED'")
    long countLeadsWithPendingTasks(Long tenantId);

    @Query("SELECT count(l) FROM CrmLead l WHERE l.tenant.id = :tenantId AND l.status NOT IN :excludedStatuses")
    long countByTenantIdAndStatusNotIn(Long tenantId, List<CrmLeadStatus> excludedStatuses);
    boolean existsByTenantIdAndLeadNo(Long tenantId, String leadNo);
}
