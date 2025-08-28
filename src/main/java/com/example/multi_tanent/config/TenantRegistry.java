// com/example/multi_tanent/config/TenantRegistry.java
package com.example.multi_tanent.config;

import com.example.multi_tanent.master.entity.MasterTenant;
import com.example.multi_tanent.master.repository.MasterTenantRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TenantRegistry {
  private final MasterTenantRepository masterRepo;
  private final ConcurrentHashMap<String, DataSource> map = new ConcurrentHashMap<>();
  private TenantRoutingDataSource routing;

  public TenantRegistry(MasterTenantRepository masterRepo) {
    this.masterRepo = masterRepo;
  }

  public void attachRouting(TenantRoutingDataSource routing) { this.routing = routing; }

  public void loadAllFromMaster() {
    masterRepo.findAll().forEach(this::addOrUpdateTenant);
  }

  public synchronized void addOrUpdateTenant(MasterTenant t) {
    HikariDataSource ds = new HikariDataSource();
    ds.setJdbcUrl(t.getJdbcUrl());
    ds.setUsername(t.getUsername());
    ds.setPassword(t.getPassword());
    map.put(t.getTenantId(), ds);
    refreshRouting();
  }

  public synchronized void removeTenant(String tenantId) {
    DataSource ds = map.remove(tenantId);
    if (ds instanceof HikariDataSource h) h.close();
    refreshRouting();
  }

  public Map<Object,Object> asTargetMap() { return new HashMap<>(map); }

  private void refreshRouting() {
    if (routing == null) return;
    var targets = asTargetMap();
    routing.setTargetDataSources(targets);
    routing.afterPropertiesSet();
  }
}
