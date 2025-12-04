package com.example.multi_tanent.crm.repository;

import com.example.multi_tanent.crm.entity.CrmSalesProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrmSalesProductRepository extends JpaRepository<CrmSalesProduct, Long> {
    List<CrmSalesProduct> findByTenantId(Long tenantId);

    org.springframework.data.domain.Page<CrmSalesProduct> findByTenantId(Long tenantId,
            org.springframework.data.domain.Pageable pageable);

    Optional<CrmSalesProduct> findByIdAndTenantId(Long id, Long tenantId);
}
