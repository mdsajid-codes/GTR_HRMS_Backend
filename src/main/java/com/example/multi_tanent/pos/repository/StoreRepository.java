package com.example.multi_tanent.pos.repository;

import com.example.multi_tanent.pos.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    List<Store> findByTenantId(Long tenantId);

    Optional<Store> findByIdAndTenantId(Long id, Long tenantId);
}