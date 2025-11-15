package com.example.multi_tanent.sales.repository;

import com.example.multi_tanent.sales.entity.SalesQuotation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalesQuotationRepository extends JpaRepository<SalesQuotation, Long> {
    boolean existsByNumber(String number);
}