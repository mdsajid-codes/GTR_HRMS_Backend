package com.example.multi_tanent.crm.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.example.multi_tanent.crm.entity.CrmNotificationTemplateEmployee;

import java.util.List;

public interface CrmNotificationTemplateEmployeeRepository extends JpaRepository<CrmNotificationTemplateEmployee, Long> {
  List<CrmNotificationTemplateEmployee> findByTemplateId(Long templateId);
  void deleteByTemplateId(Long templateId);
}

