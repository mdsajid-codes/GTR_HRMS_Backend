package com.example.multi_tanent.sales.repository;

import com.example.multi_tanent.sales.entity.SalesDeliveryOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalesDeliveryOrderLineRepository extends JpaRepository<SalesDeliveryOrderLine, Long> {
    // This can be removed as findById is sufficient in a single-tenant context
}
