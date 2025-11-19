package com.example.multi_tanent.sales.repository;

import com.example.multi_tanent.sales.entity.SaleProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SaleProductRepository extends JpaRepository<SaleProduct, Long> {
    boolean existsByTenantIdAndSkuIgnoreCase(Long tenantId, String sku);
    Optional<SaleProduct> findByTenantIdAndId(Long tenantId, Long id);
    Page<SaleProduct> findByTenantId(Long tenantId, Pageable pageable);
}