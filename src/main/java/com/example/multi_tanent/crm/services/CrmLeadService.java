// package com.example.multi_tanent.crm.services;

// import com.example.multi_tanent.config.TenantContext;
// import com.example.multi_tanent.crm.dto.CrmLeadRequest;
// import com.example.multi_tanent.crm.dto.CrmLeadResponse;
// import com.example.multi_tanent.crm.entity.CrmCompany;
// import com.example.multi_tanent.crm.entity.CrmLead;
// import com.example.multi_tanent.crm.entity.CrmLeadStage;
// import com.example.multi_tanent.crm.repository.CrmCompanyRepository;
// import com.example.multi_tanent.crm.repository.CrmLeadRepository;
// import com.example.multi_tanent.crm.repository.CrmLeadStageRepository;
// import com.example.multi_tanent.pos.repository.TenantRepository;
// import com.example.multi_tanent.spersusers.enitity.Employee;
// import com.example.multi_tanent.spersusers.enitity.Location;
// import com.example.multi_tanent.spersusers.enitity.Tenant;
// import com.example.multi_tanent.spersusers.repository.LocationRepository;
// import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository;
// import jakarta.persistence.EntityNotFoundException;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import lombok.RequiredArgsConstructor;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.List;
// import java.util.stream.Collectors;

// @Service
// @RequiredArgsConstructor
// @Transactional("tenantTx")
// public class CrmLeadService {

//     private final CrmLeadRepository leadRepository;
//     private final TenantRepository tenantRepository;
//     private final LocationRepository locationRepository;
//     private final CrmCompanyRepository companyRepository;
//     private final CrmLeadStageRepository leadStageRepository;
//     private final EmployeeRepository employeeRepository;

//     private Tenant getCurrentTenant() {
//         String tenantId = TenantContext.getTenantId();
//         return tenantRepository.findByName(tenantId) // Assuming tenant name is the identifier
//                 .orElseThrow(() -> new IllegalStateException("Tenant not found for ID: " + tenantId));
//     }

//     public CrmLeadResponse createLead(CrmLeadRequest request) {
//         Tenant tenant = getCurrentTenant();
//         CrmLead lead = new CrmLead();
//         mapRequestToEntity(request, lead, tenant);

//         // Set default stage if not provided
//         if (lead.getCurrentStage() == null) {
//             leadStageRepository.findFirstByTenantIdAndIsDefaultTrue(tenant.getId())
//                     .ifPresent(lead::setCurrentStage);
//         }

//         CrmLead savedLead = leadRepository.save(lead);
//         return toResponse(savedLead);
//     }

//     public Page<CrmLeadResponse> getAllLeads(Pageable pageable) {
//         Page<CrmLead> leadsPage = leadRepository.findByTenantId(getCurrentTenant().getId(), pageable);
//         return leadsPage.map(this::toResponse);
//     }

//     public CrmLeadResponse getLeadById(Long id) {
//         return leadRepository.findByIdAndTenantId(id, getCurrentTenant().getId())
//                 .map(this::toResponse)
//                 .orElseThrow(() -> new EntityNotFoundException("Lead not found with id: " + id));
//     }

//     public CrmLeadResponse updateLead(Long id, CrmLeadRequest request) {
//         CrmLead lead = leadRepository.findByIdAndTenantId(id, getCurrentTenant().getId())
//                 .orElseThrow(() -> new EntityNotFoundException("Lead not found with id: " + id));
//         mapRequestToEntity(request, lead, lead.getTenant());
//         CrmLead updatedLead = leadRepository.save(lead);
//         return toResponse(updatedLead);
//     }

//     public void deleteLead(Long id) {
//         if (!leadRepository.existsById(id)) {
//             throw new EntityNotFoundException("Lead not found with id: " + id);
//         }
//         leadRepository.deleteById(id);
//     }

//     private void mapRequestToEntity(CrmLeadRequest request, CrmLead lead, Tenant tenant) {
//         CrmCompany company = companyRepository.findById(request.getCompanyId())
//                 .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + request.getCompanyId()));

//         lead.setTenant(tenant);
//         lead.setFirstName(request.getFirstName());
//         lead.setLastName(request.getLastName());
//         lead.setCompany(company);
//         lead.setIndustry(request.getIndustry());
//         lead.setDesignation(request.getDesignation());
//         lead.setPhone(request.getPhone());
//         lead.setEmail(request.getEmail());
//         lead.setWebsite(request.getWebsite());
//         lead.setProducts(request.getProducts());
//         lead.setRequirements(request.getRequirements());
//         lead.setLeadSource(request.getLeadSource());
//         lead.setAddress(request.getAddress());
//         lead.setNotes(request.getNotes());

//         if (request.getLocationId() != null) {
//             Location location = locationRepository.findById(request.getLocationId())
//                     .orElseThrow(() -> new EntityNotFoundException("Location not found with id: " + request.getLocationId()));
//             lead.setLocation(location);
//         } else {
//             lead.setLocation(null);
//         }

//         if (request.getOwnerId() != null) {
//             Employee owner = employeeRepository.findById(request.getOwnerId())
//                     .orElseThrow(() -> new EntityNotFoundException("Owner (Employee) not found with id: " + request.getOwnerId()));
//             lead.setOwner(owner);
//         } else {
//             lead.setOwner(null);
//         }

//         if (request.getCurrentStageId() != null) {
//             CrmLeadStage stage = leadStageRepository.findById(request.getCurrentStageId())
//                     .orElseThrow(() -> new EntityNotFoundException("Lead Stage not found with id: " + request.getCurrentStageId()));
//             lead.setCurrentStage(stage);
//         } else {
//             lead.setCurrentStage(null);
//         }
//     }

//     private CrmLeadResponse toResponse(CrmLead lead) {
//         CrmLeadResponse.CrmLeadResponseBuilder builder = CrmLeadResponse.builder()
//                 .id(lead.getId())
//                 .firstName(lead.getFirstName())
//                 .lastName(lead.getLastName())
//                 .industry(lead.getIndustry())
//                 .designation(lead.getDesignation())
//                 .phone(lead.getPhone())
//                 .email(lead.getEmail())
//                 .website(lead.getWebsite())
//                 .products(lead.getProducts())
//                 .requirements(lead.getRequirements())
//                 .leadSource(lead.getLeadSource())
//                 .address(lead.getAddress())
//                 .notes(lead.getNotes())
//                 .createdAt(lead.getCreatedAt())
//                 .updatedAt(lead.getUpdatedAt());

//         if (lead.getCompany() != null) {
//             builder.companyId(lead.getCompany().getId()).companyName(lead.getCompany().getName());
//         }
//         if (lead.getOwner() != null) {
//             builder.ownerId(lead.getOwner().getId()).ownerName(lead.getOwner().getFirstName() + " " + lead.getOwner().getLastName());
//         }
//         if (lead.getCurrentStage() != null) {
//             builder.currentStageId(lead.getCurrentStage().getId()).currentStageName(lead.getCurrentStage().getName());
//         }
//         if (lead.getLocation() != null) {
//             builder.locationId(lead.getLocation().getId()).locationName(lead.getLocation().getName());
//         }

//         return builder.build();
//     }
// }
package com.example.multi_tanent.crm.services;

import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.crm.dto.CrmLeadRequest;
import com.example.multi_tanent.crm.dto.CrmLeadResponse;
import com.example.multi_tanent.crm.entity.*;
import com.example.multi_tanent.crm.repository.*;
import com.example.multi_tanent.pos.repository.TenantRepository;
import com.example.multi_tanent.spersusers.enitity.Employee;
import com.example.multi_tanent.spersusers.enitity.Location;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import com.example.multi_tanent.spersusers.repository.LocationRepository;
import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional("tenantTx")
public class CrmLeadService {

    private final CrmLeadRepository leadRepo;
    private final TenantRepository tenantRepo;

    private final CrmCompanyRepository companyRepo;
    private final CrmLeadStageRepository stageRepo;
    private final EmployeeRepository employeeRepo;
    private final LocationRepository locationRepo;

    private final CrmIndustryRepository industryRepo;
    private final CrmProductRepository productRepo;

    /* ---------------- tenant resolution ---------------- */

    private Tenant currentTenant() {
        String key = TenantContext.getTenantId();
        // In a "database per tenant" model, each tenant's database has exactly one
        // tenant record. We can reliably fetch it this way.
        return tenantRepo.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Tenant record not found in the current database for tenant key: " + key));
    }

    /* ---------------- CRUD ---------------- */

    public CrmLeadResponse create(CrmLeadRequest req) {
        Tenant t = currentTenant();

        CrmLead entity = new CrmLead();
        mapReqToEntity(req, entity, t);

        // default stage if not provided
        if (entity.getCurrentStage() == null) {
            stageRepo.findFirstByTenantIdAndIsDefaultTrue(t.getId())
                    .ifPresent(entity::setCurrentStage);
        }

        entity = leadRepo.save(entity);
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<CrmLeadResponse> list(Pageable pageable) {
        Long tenantId = currentTenant().getId();
        return leadRepo.findByTenantId(tenantId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CrmLeadResponse get(Long id) {
        Long tenantId = currentTenant().getId();
        CrmLead entity = leadRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Lead not found: " + id));
        return toResponse(entity);
    }

    public CrmLeadResponse update(Long id, CrmLeadRequest req) {
        Long tenantId = currentTenant().getId();
        CrmLead entity = leadRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Lead not found: " + id));

        mapReqToEntity(req, entity, entity.getTenant());
        entity = leadRepo.save(entity);
        return toResponse(entity);
    }

    public void delete(Long id) {
        Long tenantId = currentTenant().getId();
        CrmLead entity = leadRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Lead not found: " + id));
        leadRepo.delete(entity);
    }

    /* ---------------- mapping helpers ---------------- */

    private void mapReqToEntity(CrmLeadRequest r, CrmLead e, Tenant tenant) {

        CrmCompany company = companyRepo.findById(r.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Company not found: " + r.getCompanyId()));

        e.setTenant(tenant);
        e.setCompany(company);

        e.setFirstName(r.getFirstName());
        e.setLastName(r.getLastName());
        e.setDesignation(r.getDesignation());
        e.setPhone(r.getPhone());
        e.setEmail(r.getEmail());
        e.setWebsite(r.getWebsite());

        if (r.getIndustryId() != null) {
            CrmIndustry industry = industryRepo.findById(r.getIndustryId())
                    .orElseThrow(() -> new EntityNotFoundException("Industry not found: " + r.getIndustryId()));
            e.setIndustry(industry);
        } else {
            e.setIndustry(null);
        }

        // products
        Set<CrmProduct> prods = new LinkedHashSet<>();
        if (r.getProductIds() != null && !r.getProductIds().isEmpty()) {
            prods.addAll(productRepo.findAllById(r.getProductIds()));
        }
        e.setProducts(prods);

        e.setRequirements(r.getRequirements());
        e.setLeadSource(r.getLeadSource());
        e.setAddress(r.getAddress());
        e.setNotes(r.getNotes());

        if (r.getOwnerId() != null) {
            Employee owner = employeeRepo.findById(r.getOwnerId())
                    .orElseThrow(() -> new EntityNotFoundException("Owner not found: " + r.getOwnerId()));
            e.setOwner(owner);
        } else {
            e.setOwner(null);
        }

        if (r.getLocationId() != null) {
            Location loc = locationRepo.findById(r.getLocationId())
                    .orElseThrow(() -> new EntityNotFoundException("Location not found: " + r.getLocationId()));
            e.setLocation(loc);
        } else {
            e.setLocation(null);
        }

        if (r.getCurrentStageId() != null) {
            CrmLeadStage stage = stageRepo.findById(r.getCurrentStageId())
                    .orElseThrow(() -> new EntityNotFoundException("Lead stage not found: " + r.getCurrentStageId()));
            e.setCurrentStage(stage);
        } else {
            e.setCurrentStage(null);
        }

        e.setForecastCategory(r.getForecastCategory());
        e.setExpectedCloseDate(r.getExpectedCloseDate());
        e.setAmount(r.getAmount());
    }

    private CrmLeadResponse toResponse(CrmLead e) {
        var b = CrmLeadResponse.builder()
                .id(e.getId())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .designation(e.getDesignation())
                .phone(e.getPhone())
                .email(e.getEmail())
                .website(e.getWebsite())
                .requirements(e.getRequirements())
                .leadSource(e.getLeadSource())
                .address(e.getAddress())
                .notes(e.getNotes())
                .forecastCategory(e.getForecastCategory() != null ? e.getForecastCategory().name() : null)
                .expectedCloseDate(e.getExpectedCloseDate())
                .amount(e.getAmount())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt());

        if (e.getCompany() != null) {
            b.companyId(e.getCompany().getId()).companyName(e.getCompany().getName());
        }
        if (e.getIndustry() != null) {
            b.industryId(e.getIndustry().getId()).industryName(e.getIndustry().getName());
        }
        if (e.getOwner() != null) {
            b.ownerId(e.getOwner().getId())
             .ownerName(e.getOwner().getFirstName() + " " + e.getOwner().getLastName());
        }
        if (e.getCurrentStage() != null) {
            b.currentStageId(e.getCurrentStage().getId())
             .currentStageName(e.getCurrentStage().getName());
        }
        if (e.getLocation() != null) {
            b.locationId(e.getLocation().getId()).locationName(e.getLocation().getName());
        }
        if (e.getProducts() != null && !e.getProducts().isEmpty()) {
            b.products(
                e.getProducts().stream()
                    .map(p -> new CrmLeadResponse.ProductMini(p.getId(), p.getName()))
                    .collect(Collectors.toCollection(LinkedHashSet::new))
            );
        }

        return b.build();
    }
}
