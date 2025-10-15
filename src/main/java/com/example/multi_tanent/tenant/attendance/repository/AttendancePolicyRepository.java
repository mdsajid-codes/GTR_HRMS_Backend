package com.example.multi_tanent.tenant.attendance.repository;

import com.example.multi_tanent.tenant.attendance.entity.AttendancePolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendancePolicyRepository extends JpaRepository<AttendancePolicy, Long> {

    Optional<AttendancePolicy> findByIsDefaultTrue();

    Optional<AttendancePolicy> findByPolicyName(String policyName);

}