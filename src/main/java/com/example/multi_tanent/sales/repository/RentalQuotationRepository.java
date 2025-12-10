package com.example.multi_tanent.sales.repository;

import com.example.multi_tanent.sales.entity.RentalQuotation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RentalQuotationRepository extends JpaRepository<RentalQuotation, Long> {
    Page<RentalQuotation> findByTenantId(Long tenantId, Pageable pageable);

    Optional<RentalQuotation> findByIdAndTenantId(Long id, Long tenantId);
}
