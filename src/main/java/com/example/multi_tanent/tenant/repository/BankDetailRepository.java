package com.example.multi_tanent.tenant.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.multi_tanent.tenant.entity.BankDetails;

public interface BankDetailRepository extends JpaRepository<BankDetails, Long> {
    
}
