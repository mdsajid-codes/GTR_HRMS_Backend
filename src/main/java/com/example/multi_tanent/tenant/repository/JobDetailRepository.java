package com.example.multi_tanent.tenant.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.multi_tanent.tenant.entity.JobDetails;
import com.example.multi_tanent.tenant.entity.enums.EmploymentType;

public interface JobDetailRepository extends JpaRepository<JobDetails, Long> {
    Optional<JobDetails> findByEmployeeId(Long employeeId);
    List<JobDetails> findByEmploymentType (EmploymentType employmentType);
}
