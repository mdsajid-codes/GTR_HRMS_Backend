package com.example.multi_tanent.sales.repository;

import com.example.multi_tanent.sales.entity.SalesPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalesPaymentRepository extends JpaRepository<SalesPayment, Long> {
    boolean existsByNumber(String number);
}
