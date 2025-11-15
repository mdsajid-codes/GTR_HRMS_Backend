package com.example.multi_tanent.sales.repository;

import com.example.multi_tanent.sales.entity.SaleProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SaleProductRepository extends JpaRepository<SaleProduct, Long> {

    boolean existsBySku(String sku);
}
