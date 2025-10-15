package com.example.multi_tanent.tenant.leave.repository;

import com.example.multi_tanent.tenant.leave.entity.HolidayPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayPolicyRepository extends JpaRepository<HolidayPolicy, Long> {

}