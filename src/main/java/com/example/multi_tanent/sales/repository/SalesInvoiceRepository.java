package com.example.multi_tanent.sales.repository;

import com.example.multi_tanent.sales.entity.SalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Long> {
    boolean existsByNumber(String number);
}