package com.example.multi_tanent.production.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.multi_tanent.production.entity.ProInventoryType;

public interface ProInventoryTypeRepository extends JpaRepository<ProInventoryType, Long> {
    
}
