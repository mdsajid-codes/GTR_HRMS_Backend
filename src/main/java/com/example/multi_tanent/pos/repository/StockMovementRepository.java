package com.example.multi_tanent.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.multi_tanent.pos.entity.StockMovement;

import java.util.List;
import java.util.Optional;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByTenantId(Long tenantId);
    Optional<StockMovement> findByIdAndTenantId(Long id, Long tenantId);

    /**
     * Deletes all stock movement records associated with a given list of product variant IDs.
     * This is used to clean up history before deleting a product.
     * @param productVariantIds A list of product variant IDs.
     */
    void deleteByProductVariantIdIn(List<Long> productVariantIds);
}
