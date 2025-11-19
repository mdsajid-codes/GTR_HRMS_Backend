package com.example.multi_tanent.sales.repository;

import com.example.multi_tanent.sales.entity.SalesTermAndCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SalesTermAndConditionRepository extends JpaRepository<SalesTermAndCondition, Long> {
    List<SalesTermAndCondition> findByTenantIdOrderByNameAsc(Long tenantId);
    Optional<SalesTermAndCondition> findByTenantIdAndId(Long tenantId, Long id);
    boolean existsByTenantIdAndNameIgnoreCase(Long tenantId, String name);

    @Modifying
    @Query("UPDATE SalesTermAndCondition t SET t.isDefault = false WHERE t.tenant.id = :tenantId AND t.isDefault = true AND t.id <> :excludeId")
    void clearDefaultFlagForTenant(Long tenantId, Long excludeId);
}