package com.example.multi_tanent.sales.repository;

import com.example.multi_tanent.sales.entity.SalesOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine, Long> {
    // This can be removed as findById is sufficient in a single-tenant context
}
