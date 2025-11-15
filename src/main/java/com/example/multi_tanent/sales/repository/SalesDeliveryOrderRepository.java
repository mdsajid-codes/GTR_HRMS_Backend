package com.example.multi_tanent.sales.repository;

import com.example.multi_tanent.sales.entity.SalesDeliveryOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalesDeliveryOrderRepository extends JpaRepository<SalesDeliveryOrder, Long> {
    boolean existsByNumber(String number);
}
