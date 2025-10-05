package com.example.multi_tanent.pos.service;

import com.example.multi_tanent.pos.dto.UpdateTenantRequest;
import com.example.multi_tanent.pos.dto.TenantDto;
import com.example.multi_tanent.pos.repository.TenantRepository;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@Transactional("tenantTx")
public class TenantService {

    private final TenantRepository tenantRepository;
    private final FileStorageService fileStorageService;

    public TenantService(TenantRepository tenantRepository,
                         @Qualifier("posFileStorageService") FileStorageService fileStorageService) {
        this.tenantRepository = tenantRepository;
        this.fileStorageService = fileStorageService;
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

        // Allow updating other fields. We check for null so clients can update only specific fields.
        if (updateRequest.getLogoImgUrl() != null) {
            tenant.setLogoImgUrl(updateRequest.getLogoImgUrl());
        }
        if (updateRequest.getContactEmail() != null) {
            tenant.setContactEmail(updateRequest.getContactEmail());
        }
        if (updateRequest.getContactPhone() != null) {
            tenant.setContactPhone(updateRequest.getContactPhone());
        }
        if (updateRequest.getAddress() != null) {
            tenant.setAddress(updateRequest.getAddress());
        }

        return tenantRepository.save(tenant);
    }

    /**
     * Updates the logo for the current tenant.
     *
     * @param file The logo image file to upload.
     * @return The updated Tenant entity with the new logo URL.
     */
    public Tenant updateTenantLogo(MultipartFile file) {
        try {
            Tenant tenant = getCurrentTenant()
                    .orElseThrow(() -> new IllegalStateException("Tenant record not found in the current tenant database."));
    
            // Store tenant logos in a public location not tied to a tenant ID folder.
            // This requires a method that doesn't rely on TenantContext.
            // The path will be like 'tenant-assets/logos/some-uuid.png'
            String logoPath = fileStorageService.storePublicFile(file.getBytes(), file.getOriginalFilename(), "tenant-assets/logos");
            
            tenant.setLogoImgUrl(logoPath);
            return tenantRepository.save(tenant);
        } catch (IOException e) {
            throw new RuntimeException("Could not read file for logo upload.", e);
        }
    }

    public TenantDto toDto(Tenant tenant) {
        return TenantDto.fromEntity(tenant);
    }
}