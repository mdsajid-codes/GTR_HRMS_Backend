package com.example.multi_tanent.sales.repository;

import com.example.multi_tanent.sales.entity.SalePriceListItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalePriceListItemRepository extends JpaRepository<SalePriceListItem, Long> {
    void deleteByPriceListId(Long priceListId);

    List<SalePriceListItem> findByPriceListId(Long priceListId);
}