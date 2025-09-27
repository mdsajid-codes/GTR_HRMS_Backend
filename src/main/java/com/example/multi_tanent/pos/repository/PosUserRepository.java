package com.example.multi_tanent.pos.repository;

import com.example.multi_tanent.pos.entity.PosUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PosUserRepository extends JpaRepository<PosUser, Long> {
    List<PosUser> findByTenantId(Long tenantId);

    Optional<PosUser> findByIdAndTenantId(Long id, Long tenantId);

    Optional<PosUser> findByEmailAndTenantId(String email, Long tenantId);
}