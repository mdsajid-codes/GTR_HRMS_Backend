package com.example.multi_tanent.spersusers.repository;

import com.example.multi_tanent.spersusers.base.PartyBase.PartyType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import com.example.multi_tanent.spersusers.enitity.BaseCustomer;

public interface PartyRepository extends JpaRepository<BaseCustomer, Long> {
    Page<BaseCustomer> findByTenantId(Long tenantId, Pageable pageable);
    Optional<BaseCustomer> findByTenantIdAndId(Long tenantId, Long id);
    Page<BaseCustomer> findByTenantIdAndPartyType(Long tenantId, PartyType partyType, Pageable pageable);
}