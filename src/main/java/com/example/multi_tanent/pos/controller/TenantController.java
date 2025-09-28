package com.example.multi_tanent.pos.controller;

import com.example.multi_tanent.pos.dto.UpdateTenantRequest;
import com.example.multi_tanent.pos.service.TenantService;
import com.example.multi_tanent.spersusers.enitity.Tenant;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pos/tenant")
@CrossOrigin(origins = "*")
public class TenantController {
    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','POS_ADMIN', 'POS_MANAGER')")
    public ResponseEntity<Tenant> getCurrentTenant() {
        return tenantService.getCurrentTenant().map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/current")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','POS_ADMIN')")
    public ResponseEntity<Tenant> updateTenant(@RequestBody UpdateTenantRequest updateRequest) {
        return ResponseEntity.ok(tenantService.updateCurrentTenant(updateRequest));
    }
}
