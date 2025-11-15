package com.example.multi_tanent.sales.repository;

import com.example.multi_tanent.sales.entity.SalePriceList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SalePriceListRepository extends JpaRepository<SalePriceList, Long> {
    boolean existsByName(String name);
    @Modifying
    @Query("UPDATE SalePriceList p SET p.isDefault = false WHERE p.id <> :excludedId")
    void unsetAllDefaults(Long excludedId);
}
