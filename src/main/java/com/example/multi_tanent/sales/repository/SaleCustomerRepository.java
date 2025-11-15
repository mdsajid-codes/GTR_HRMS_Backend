package com.example.multi_tanent.sales.repository;

import com.example.multi_tanent.sales.entity.SaleCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SaleCustomerRepository extends JpaRepository<SaleCustomer, Long> {
    boolean existsByCode(String code);

}
