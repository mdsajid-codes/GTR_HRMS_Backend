package com.example.multi_tanent.pos.service;

import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.pos.dto.UpdateTenantRequest;
import com.example.multi_tanent.spersusers.dto.TenantDto;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import com.example.multi_tanent.spersusers.repository.TenantRepository;
import com.example.multi_tanent.tenant.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@Transactional(transactionManager = "tenantTx")
public class TenantService {
    private final TenantRepository tenantRepository;
    private final FileStorageService fileStorageService;

    public TenantService(TenantRepository tenantRepository, FileStorageService fileStorageService) {
        this.tenantRepository = tenantRepository;
        this.fileStorageService = fileStorageService;
    }

    public Optional<Tenant> getCurrentTenant() {
        // Correctly fetch the single tenant record from the tenant's database.
        return tenantRepository.findAll().stream().findFirst();
    }

    public Tenant updateCurrentTenant(UpdateTenantRequest updateRequest) {
        Tenant tenant = getCurrentTenant().orElseThrow(() -> new IllegalStateException("Tenant not found in current context."));
        tenant.setContactEmail(updateRequest.getContactEmail());
        tenant.setContactPhone(updateRequest.getContactPhone());
        tenant.setAddress(updateRequest.getAddress());
        return tenantRepository.save(tenant);
    }

    public Tenant updateTenantLogo(MultipartFile file) {
        Tenant tenant = getCurrentTenant().orElseThrow(() -> new IllegalStateException("Tenant not found in current context."));
        String fileName = fileStorageService.storeFile(file, TenantContext.getTenantId() + "_logo");
        tenant.setLogoImgUrl(fileName);
        return tenantRepository.save(tenant);
    }

    public TenantDto toDto(Tenant tenant) {
        TenantDto dto = new TenantDto();
        dto.setId(tenant.getId());
        dto.setName(tenant.getName());
        dto.setLogoImgUrl(tenant.getLogoImgUrl());
        dto.setContactEmail(tenant.getContactEmail());
        dto.setContactPhone(tenant.getContactPhone());
        dto.setAddress(tenant.getAddress());
        return dto;
    }
}