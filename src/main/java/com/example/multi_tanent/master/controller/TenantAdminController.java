package com.example.multi_tanent.master.controller;

import com.example.multi_tanent.master.entity.MasterTenant;
import com.example.multi_tanent.master.repository.MasterTenantRepository;
import com.example.multi_tanent.config.TenantRegistry;
import com.example.multi_tanent.config.TenantSchemaCreator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/master/tenants")
public class TenantAdminController {
  private final MasterTenantRepository repo;
  private final TenantRegistry registry;
  private final TenantSchemaCreator schema;

  public TenantAdminController(MasterTenantRepository repo, TenantRegistry registry, TenantSchemaCreator schema) {
    this.repo = repo; this.registry = registry; this.schema = schema;
  }

  @PostMapping
  @PreAuthorize("hasRole('MASTER_ADMIN')")
  @Transactional
  public String createTenant(@RequestBody MasterTenant t) {
    repo.save(t);
    registry.addOrUpdateTenant(t);
    // ensure schema for this tenant
    schema.ensureSchema(registry.asTargetMap().get(t.getTenantId()));
    return "created";
  }

  @DeleteMapping("/{tenantId}")
  @PreAuthorize("hasRole('MASTER_ADMIN')")
  @Transactional
  public String deleteTenant(@PathVariable String tenantId) {
    repo.findByTenantId(tenantId).ifPresent(repo::delete);
    registry.removeTenant(tenantId);
    return "deleted";
  }
}