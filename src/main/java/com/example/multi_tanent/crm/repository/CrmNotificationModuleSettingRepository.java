package com.example.multi_tanent.crm.repository;



import com.example.multi_tanent.crm.entity.CrmNotificationModuleSetting;
import com.example.multi_tanent.crm.enums.NotificationModule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CrmNotificationModuleSettingRepository extends JpaRepository<CrmNotificationModuleSetting, Long> {
  Optional<CrmNotificationModuleSetting> findByTenantIdAndModule(Long tenantId, NotificationModule module);
}

