package com.example.multi_tanent.sales.repository;

import com.example.multi_tanent.sales.entity.SalesAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesAuditLogRepository extends JpaRepository<SalesAuditLog, Long> {

    List<SalesAuditLog> findByEntityNameAndEntityIdOrderByTimestampDesc(String entityName, Long entityId);

}
