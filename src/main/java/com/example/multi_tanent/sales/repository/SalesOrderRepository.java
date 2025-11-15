package com.example.multi_tanent.sales.repository;

import com.example.multi_tanent.sales.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    boolean existsByNumber(String number);
}
