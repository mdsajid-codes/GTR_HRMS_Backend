package com.example.multi_tanent.crm.services;

import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.crm.dto.CrmCompanyRequest;
import com.example.multi_tanent.crm.dto.CrmCompanyResponse;
import com.example.multi_tanent.crm.entity.CompanyType;
import com.example.multi_tanent.crm.entity.CrmCompany;
import com.example.multi_tanent.crm.entity.CrmIndustry;
import com.example.multi_tanent.crm.repository.CrmCompanyRepository;
import com.example.multi_tanent.crm.repository.CrmCompanyTypeRepository;
import com.example.multi_tanent.crm.repository.CrmIndustryRepository;
import com.example.multi_tanent.pos.repository.TenantRepository;
import com.example.multi_tanent.spersusers.enitity.Location;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import com.example.multi_tanent.spersusers.repository.LocationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional("tenantTx")
public class CrmCompanyService {

    private final CrmCompanyRepository companyRepository;
    private final TenantRepository tenantRepository;
    private final LocationRepository locationRepository;
    private final CrmCompanyTypeRepository companyTypeRepository;
    private final CrmIndustryRepository industryRepository;

    private Tenant getCurrentTenant() {
        String tenantId = TenantContext.getTenantId();
        return tenantRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Tenant not found for tenantId: " + tenantId));
    }

    public CrmCompanyResponse createCompany(CrmCompanyRequest request) {
        Tenant tenant = getCurrentTenant();
        if (companyRepository.existsByTenantIdAndNameIgnoreCase(tenant.getId(), request.getName())) {
            throw new IllegalArgumentException("A company with this name already exists.");
        }
        CrmCompany company = new CrmCompany();
        mapRequestToEntity(request, company, tenant);
        CrmCompany savedCompany = companyRepository.save(company);
        return toResponse(savedCompany);
    }

    public List<CrmCompanyResponse> getAllCompanies() {
        return companyRepository.findByTenantId(getCurrentTenant().getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CrmCompanyResponse getCompanyById(Long id) {
        return companyRepository.findByIdAndTenantId(id, getCurrentTenant().getId())
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + id));
    }

    public CrmCompanyResponse updateCompany(Long id, CrmCompanyRequest request) {
        CrmCompany company = companyRepository.findByIdAndTenantId(id, getCurrentTenant().getId())
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + id));

        if (!company.getName().equalsIgnoreCase(request.getName()) &&
            companyRepository.existsByTenantIdAndNameIgnoreCase(company.getTenant().getId(), request.getName())) {
            throw new IllegalArgumentException("A company with this name already exists.");
        }

        mapRequestToEntity(request, company, company.getTenant());
        CrmCompany updatedCompany = companyRepository.save(company);
        return toResponse(updatedCompany);
    }

    public void deleteCompany(Long id) {
        if (!companyRepository.existsById(id)) {
            throw new EntityNotFoundException("Company not found with id: " + id);
        }
        companyRepository.deleteById(id);
    }

    private void mapRequestToEntity(CrmCompanyRequest request, CrmCompany company, Tenant tenant) {
        company.setTenant(tenant);
        company.setName(request.getName());
        company.setPhone(request.getPhone());
        company.setEmail(request.getEmail());
        company.setWebsite(request.getWebsite());
        company.setAddress(request.getAddress());

        if (request.getLocationId() != null) {
            Location location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new EntityNotFoundException("Location not found with id: " + request.getLocationId()));
            company.setLocation(location);
        } else {
            company.setLocation(null);
        }

        if (request.getCompanyTypeId() != null) {
            CompanyType companyType = companyTypeRepository.findById(request.getCompanyTypeId())
                    .orElseThrow(() -> new EntityNotFoundException("Company Type not found with id: " + request.getCompanyTypeId()));
            company.setCompanyType(companyType);
        } else {
            company.setCompanyType(null);
        }

        if (request.getIndustryId() != null) {
            CrmIndustry industry = industryRepository.findById(request.getIndustryId())
                    .orElseThrow(() -> new EntityNotFoundException("Industry not found with id: " + request.getIndustryId()));
            company.setIndustry(industry);
        } else {
            company.setIndustry(null);
        }
    }

    private CrmCompanyResponse toResponse(CrmCompany company) {
        CrmCompanyResponse.CrmCompanyResponseBuilder builder = CrmCompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .phone(company.getPhone())
                .email(company.getEmail())
                .website(company.getWebsite())
                .address(company.getAddress())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt());

        if (company.getLocation() != null) {
            builder.locationId(company.getLocation().getId()).locationName(company.getLocation().getName());
        }
        if (company.getCompanyType() != null) {
            builder.companyTypeId(company.getCompanyType().getId()).companyTypeName(company.getCompanyType().getName());
        }
        if (company.getIndustry() != null) {
            builder.industryId(company.getIndustry().getId()).industryName(company.getIndustry().getName());
        }

        return builder.build();
    }
}