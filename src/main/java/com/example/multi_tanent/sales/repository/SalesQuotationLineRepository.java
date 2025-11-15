package com.example.multi_tanent.sales.repository;

import com.example.multi_tanent.sales.entity.SalesQuotationLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalesQuotationLineRepository extends JpaRepository<SalesQuotationLine, Long> {
    List<SalesQuotationLine> findByQuotationId(Long quotationId);
}