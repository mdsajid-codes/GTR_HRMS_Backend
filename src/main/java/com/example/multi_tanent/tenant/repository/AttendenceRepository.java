package com.example.multi_tanent.tenant.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.multi_tanent.tenant.entity.Attendence;

public interface AttendenceRepository extends JpaRepository<Attendence, Long> {
    
}
