package com.example.multi_tanent.crm.services;

import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.crm.dto.CrmLeadRequest;
import com.example.multi_tanent.crm.dto.CrmLeadResponse;
import com.example.multi_tanent.crm.entity.CrmCompany;
import com.example.multi_tanent.crm.entity.CrmLead;
import com.example.multi_tanent.crm.entity.CrmLeadStage;
import com.example.multi_tanent.crm.repository.CrmCompanyRepository;
import com.example.multi_tanent.crm.repository.CrmLeadRepository;
import com.example.multi_tanent.crm.repository.CrmLeadStageRepository;
import com.example.multi_tanent.pos.repository.TenantRepository;
import com.example.multi_tanent.spersusers.enitity.Employee;
import com.example.multi_tanent.spersusers.enitity.Location;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import com.example.multi_tanent.spersusers.repository.LocationRepository;
import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional("tenantTx")
public class CrmLeadService {

    private final CrmLeadRepository leadRepository;
    private final TenantRepository tenantRepository;
    private final LocationRepository locationRepository;
    private final CrmCompanyRepository companyRepository;
    private final CrmLeadStageRepository leadStageRepository;
    private final EmployeeRepository employeeRepository;

    private Tenant getCurrentTenant() {
        String tenantId = TenantContext.getTenantId();
        return tenantRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Tenant not found for tenantId: " + tenantId));
    }

    public CrmLeadResponse createLead(CrmLeadRequest request) {
        Tenant tenant = getCurrentTenant();
        CrmLead lead = new CrmLead();
        mapRequestToEntity(request, lead, tenant);

        // Set default stage if not provided
        if (lead.getCurrentStage() == null) {
            leadStageRepository.findFirstByTenantIdAndIsDefaultTrue(tenant.getId())
                    .ifPresent(lead::setCurrentStage);
        }

        CrmLead savedLead = leadRepository.save(lead);
        return toResponse(savedLead);
    }

    public Page<CrmLeadResponse> getAllLeads(Pageable pageable) {
        Page<CrmLead> leadsPage = leadRepository.findByTenantId(getCurrentTenant().getId(), pageable);
        return leadsPage.map(this::toResponse);
    }

    public CrmLeadResponse getLeadById(Long id) {
        return leadRepository.findByIdAndTenantId(id, getCurrentTenant().getId())
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Lead not found with id: " + id));
    }

    public CrmLeadResponse updateLead(Long id, CrmLeadRequest request) {
        CrmLead lead = leadRepository.findByIdAndTenantId(id, getCurrentTenant().getId())
                .orElseThrow(() -> new EntityNotFoundException("Lead not found with id: " + id));
        mapRequestToEntity(request, lead, lead.getTenant());
        CrmLead updatedLead = leadRepository.save(lead);
        return toResponse(updatedLead);
    }

    public void deleteLead(Long id) {
        if (!leadRepository.existsById(id)) {
            throw new EntityNotFoundException("Lead not found with id: " + id);
        }
        leadRepository.deleteById(id);
    }

    private void mapRequestToEntity(CrmLeadRequest request, CrmLead lead, Tenant tenant) {
        CrmCompany company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + request.getCompanyId()));

        lead.setTenant(tenant);
        lead.setFirstName(request.getFirstName());
        lead.setLastName(request.getLastName());
        lead.setCompany(company);
        lead.setIndustry(request.getIndustry());
        lead.setDesignation(request.getDesignation());
        lead.setPhone(request.getPhone());
        lead.setEmail(request.getEmail());
        lead.setWebsite(request.getWebsite());
        lead.setProducts(request.getProducts());
        lead.setRequirements(request.getRequirements());
        lead.setLeadSource(request.getLeadSource());
        lead.setAddress(request.getAddress());
        lead.setNotes(request.getNotes());

        if (request.getLocationId() != null) {
            Location location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new EntityNotFoundException("Location not found with id: " + request.getLocationId()));
            lead.setLocation(location);
        } else {
            lead.setLocation(null);
        }

        if (request.getOwnerId() != null) {
            Employee owner = employeeRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new EntityNotFoundException("Owner (Employee) not found with id: " + request.getOwnerId()));
            lead.setOwner(owner);
        } else {
            lead.setOwner(null);
        }

        if (request.getCurrentStageId() != null) {
            CrmLeadStage stage = leadStageRepository.findById(request.getCurrentStageId())
                    .orElseThrow(() -> new EntityNotFoundException("Lead Stage not found with id: " + request.getCurrentStageId()));
            lead.setCurrentStage(stage);
        } else {
            lead.setCurrentStage(null);
        }
    }

    private CrmLeadResponse toResponse(CrmLead lead) {
        CrmLeadResponse.CrmLeadResponseBuilder builder = CrmLeadResponse.builder()
                .id(lead.getId())
                .firstName(lead.getFirstName())
                .lastName(lead.getLastName())
                .industry(lead.getIndustry())
                .designation(lead.getDesignation())
                .phone(lead.getPhone())
                .email(lead.getEmail())
                .website(lead.getWebsite())
                .products(lead.getProducts())
                .requirements(lead.getRequirements())
                .leadSource(lead.getLeadSource())
                .address(lead.getAddress())
                .notes(lead.getNotes())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt());

        if (lead.getCompany() != null) {
            builder.companyId(lead.getCompany().getId()).companyName(lead.getCompany().getName());
        }
        if (lead.getOwner() != null) {
            builder.ownerId(lead.getOwner().getId()).ownerName(lead.getOwner().getFirstName() + " " + lead.getOwner().getLastName());
        }
        if (lead.getCurrentStage() != null) {
            builder.currentStageId(lead.getCurrentStage().getId()).currentStageName(lead.getCurrentStage().getName());
        }
        if (lead.getLocation() != null) {
            builder.locationId(lead.getLocation().getId()).locationName(lead.getLocation().getName());
        }

        return builder.build();
    }
}