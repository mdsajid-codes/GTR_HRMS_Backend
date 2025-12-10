package com.example.multi_tanent.sales.repository;

import com.example.multi_tanent.sales.entity.SalesInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Long> {

    Optional<SalesInvoice> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT s FROM SalesInvoice s WHERE s.tenant.id = :tenantId " +
            "AND (:search IS NULL OR LOWER(s.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(s.reference) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(s.customer.companyName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:fromDate IS NULL OR s.invoiceDate >= :fromDate) " +
            "AND (:toDate IS NULL OR s.invoiceDate <= :toDate) " +
            "AND (:salespersonId IS NULL OR s.salesperson.id = :salespersonId)")
    Page<SalesInvoice> searchSalesInvoices(Long tenantId, String search, LocalDate fromDate, LocalDate toDate,
            Long salespersonId, Pageable pageable);
}
