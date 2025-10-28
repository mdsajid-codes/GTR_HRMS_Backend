package com.example.multi_tanent.crm.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.crm.dto.CompanyTypeRequest;
import com.example.multi_tanent.crm.entity.CompanyType;
import com.example.multi_tanent.crm.repository.CrmCompanyTypeRepository;
import com.example.multi_tanent.pos.repository.TenantRepository;
import com.example.multi_tanent.spersusers.enitity.Tenant;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional("tenantTx")
public class CrmCompanyTypeService {

    private final CrmCompanyTypeRepository companyTypeRepository;
    private final TenantRepository tenantRepository; // adjust to your project

    private Tenant getCurrentTenant() {
        // Use the TenantContext to get the current tenant from the JWT
        String tenantId = TenantContext.getTenantId();
        return tenantRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Tenant not found in current DB for tenantId: " + tenantId));
    }

    public List<CompanyType> getAllCompanyTypes() {
        Tenant tenant = getCurrentTenant();
        return companyTypeRepository.findByTenantIdOrderByNameAsc(tenant.getId());
    }

    public CompanyType getCompanyTypeById(Long id) {
        Tenant tenant = getCurrentTenant();
        return companyTypeRepository.findByIdAndTenantId(id, tenant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Company type not found"));
    }

    public CompanyType createCompanyType(CompanyTypeRequest request) {
        Tenant tenant = getCurrentTenant();

        if (companyTypeRepository.existsByTenantIdAndNameIgnoreCase(tenant.getId(), request.getName())) {
            throw new IllegalArgumentException("Company type already exists for this tenant");
        }

        CompanyType companyType = CompanyType.builder()
                .tenant(tenant)
                .name(request.getName().trim())
                .build();

        return companyTypeRepository.save(companyType);
    }

    public CompanyType updateCompanyType(Long id, CompanyTypeRequest request) {
        Tenant tenant = getCurrentTenant();
        CompanyType companyType = companyTypeRepository.findByIdAndTenantId(id, tenant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Company type not found"));

        String newName = request.getName().trim();
        if (!companyType.getName().equalsIgnoreCase(newName)
                && companyTypeRepository.existsByTenantIdAndNameIgnoreCase(tenant.getId(), newName)) {
            throw new IllegalArgumentException("Company type name already exists");
        }

        companyType.setName(newName);
        return companyTypeRepository.save(companyType);
    }

    public void deleteCompanyType(Long id) {
        Tenant tenant = getCurrentTenant();
        CompanyType companyType = companyTypeRepository.findByIdAndTenantId(id, tenant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Company type not found"));
        companyTypeRepository.delete(companyType);
    }
}
