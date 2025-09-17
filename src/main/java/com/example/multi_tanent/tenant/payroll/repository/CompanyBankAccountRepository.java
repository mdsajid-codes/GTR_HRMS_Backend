package com.example.multi_tanent.tenant.payroll.repository;

import com.example.multi_tanent.tenant.payroll.entity.CompanyBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyBankAccountRepository extends JpaRepository<CompanyBankAccount, Long> {
}