package com.example.multi_tanent.crm.services;


import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.multi_tanent.crm.dto.CrmIndustryDto;
import com.example.multi_tanent.crm.entity.CrmIndustry;
import com.example.multi_tanent.crm.repository.CrmIndustryRepository;
import com.example.multi_tanent.pos.repository.TenantRepository;
import com.example.multi_tanent.spersusers.enitity.Tenant;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional("tenantTx")
public class CrmIndustryService {

  private final CrmIndustryRepository industryRepository;
  private final TenantRepository tenantRepository;

  private Tenant getCurrentTenant() {
    return tenantRepository.findFirstByOrderByIdAsc()
        .orElseThrow(() -> new IllegalStateException("Tenant not found"));
  }

  @Transactional(readOnly = true)
  public List<CrmIndustry> getAllIndustries() {
    Tenant t = getCurrentTenant();
    return industryRepository.findByTenantIdOrderByNameAsc(t.getId());
  }

  @Transactional(readOnly = true)
  public CrmIndustry getIndustryById(Long id) {
    Tenant t = getCurrentTenant();
    return industryRepository.findByIdAndTenantId(id, t.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Industry not found with id: " + id));
  }

  public CrmIndustry create(CrmIndustryDto req) {
    Tenant t = getCurrentTenant();
    String name = req.getName().trim();

    if (industryRepository.existsByTenantIdAndNameIgnoreCase(t.getId(), name)) {
      throw new IllegalArgumentException("Industry already exists for this tenant");
    }

    CrmIndustry e = CrmIndustry.builder()
        .tenant(t)
        .name(name)
        .build();
    return industryRepository.save(e);
  }

  public CrmIndustry update(Long id, CrmIndustryDto req) {
    Tenant t = getCurrentTenant();
    CrmIndustry e = industryRepository.findByIdAndTenantId(id, t.getId())
        .orElseThrow(() -> new IllegalArgumentException("Industry not found"));

    String newName = req.getName().trim();
    if (!e.getName().equalsIgnoreCase(newName)
        && industryRepository.existsByTenantIdAndNameIgnoreCase(t.getId(), newName)) {
      throw new IllegalArgumentException("Industry name already exists");
    }

    e.setName(newName);
    return industryRepository.save(e);
  }

  public void delete(Long id) {
    Tenant t = getCurrentTenant();
    CrmIndustry e = industryRepository.findByIdAndTenantId(id, t.getId())
        .orElseThrow(() -> new IllegalArgumentException("Industry not found"));
    industryRepository.delete(e);
  }
}
