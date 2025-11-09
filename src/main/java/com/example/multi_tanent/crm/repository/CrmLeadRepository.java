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
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CrmLeadRepository extends JpaRepository<CrmLead, Long> {

    Page<CrmLead> findByTenantId(Long tenantId, Pageable pageable);

    Optional<CrmLead> findByIdAndTenantId(Long id, Long tenantId);
}
