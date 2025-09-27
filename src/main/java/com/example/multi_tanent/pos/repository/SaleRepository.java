package com.example.multi_tanent.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.multi_tanent.pos.entity.Sale;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByTenantId(Long tenantId);
    Optional<Sale> findByIdAndTenantId(Long id, Long tenantId);
}
