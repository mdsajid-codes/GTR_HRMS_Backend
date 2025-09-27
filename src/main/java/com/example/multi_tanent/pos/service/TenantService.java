package com.example.multi_tanent.pos.service;

import com.example.multi_tanent.pos.dto.UpdateTenantRequest;
import com.example.multi_tanent.pos.entity.Tenant;
import com.example.multi_tanent.pos.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional("tenantTx")
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public Optional<Tenant> getCurrentTenant() {
        // In a single-tenant DB context, there should only be one tenant record.
        return tenantRepository.findFirstByOrderByIdAsc();
    }

    public Tenant updateCurrentTenant(UpdateTenantRequest updateRequest) {
        Tenant tenant = getCurrentTenant()
                .orElseThrow(() -> new IllegalStateException("Tenant record not found in the current tenant database."));

        if (updateRequest.getName() != null && !updateRequest.getName().isBlank()) {
            tenant.setName(updateRequest.getName());
        }

        return tenantRepository.save(tenant);
    }
}