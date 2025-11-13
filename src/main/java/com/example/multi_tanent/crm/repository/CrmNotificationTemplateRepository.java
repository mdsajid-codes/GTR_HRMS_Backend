package com.example.multi_tanent.crm.repository;




import org.springframework.data.jpa.repository.JpaRepository;

import com.example.multi_tanent.crm.entity.CrmNotificationTemplate;
import com.example.multi_tanent.crm.enums.NotificationEvent;
import com.example.multi_tanent.crm.enums.NotificationModule;

import java.util.List;
import java.util.Optional;

public interface CrmNotificationTemplateRepository extends JpaRepository<CrmNotificationTemplate, Long> {
  List<CrmNotificationTemplate> findByTenantIdAndModuleOrderByEventAscIdAsc(Long tenantId, NotificationModule module);
  Optional<CrmNotificationTemplate> findByIdAndTenantId(Long id, Long tenantId);
  boolean existsByTenantIdAndModuleAndEventAndMessageTypeIgnoreCase(Long tenantId, NotificationModule m, NotificationEvent e, String messageType);
}
