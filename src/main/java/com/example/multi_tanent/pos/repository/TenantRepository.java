package com.example.multi_tanent.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.multi_tanent.spersusers.enitity.Tenant;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    // Since each tenant DB has one tenant record, we can just find the first one.
    Optional<Tenant> findFirstByOrderByIdAsc();
}