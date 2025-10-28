package com.example.multi_tanent.crm.services;



import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.crm.dto.CrmProductDto;
import com.example.multi_tanent.crm.dto.CrmProductResponse;
import com.example.multi_tanent.crm.entity.CrmIndustry;
import com.example.multi_tanent.crm.entity.CrmProduct;
import com.example.multi_tanent.crm.repository.CrmIndustryRepository;
import com.example.multi_tanent.crm.repository.CrmProductRepository;
import com.example.multi_tanent.pos.repository.TenantRepository;
import com.example.multi_tanent.spersusers.enitity.Tenant;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional("tenantTx")
public class CrmProductService {

  private final CrmProductRepository productRepository;
  private final CrmIndustryRepository industryRepository;
  private final TenantRepository tenantRepository;

  private Tenant getCurrentTenant() {
    String tenantId = TenantContext.getTenantId();
    return tenantRepository.findFirstByOrderByIdAsc()
        .orElseThrow(() -> new IllegalStateException("Tenant not found in current DB for tenantId: " + tenantId));
  }

  @Transactional(readOnly = true)
  public List<CrmProductResponse> list(Long industryId) {
    Tenant t = getCurrentTenant();
    List<CrmProduct> products;
    if (industryId != null) {
      products = productRepository.findByTenantIdAndIndustryIdOrderByNameAsc(t.getId(), industryId);
    } else {
      products = productRepository.findByTenantIdOrderByNameAsc(t.getId());
    }
    return products.stream().map(this::toDto).collect(Collectors.toList());
  }
  @Transactional(readOnly = true)
  public CrmProductResponse getById(Long id) {
    Tenant t = getCurrentTenant();
    return productRepository.findByIdAndTenantId(id, t.getId())
        .map(this::toDto)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
  }

  public CrmProductResponse create(CrmProductDto req) {
    Tenant t = getCurrentTenant();
    String name = req.getName().trim();

    if (productRepository.existsByTenantIdAndNameIgnoreCase(t.getId(), name)) {
      throw new IllegalArgumentException("Product already exists for this tenant");
    }

    CrmIndustry industry = industryRepository.findByIdAndTenantId(req.getIndustryId(), t.getId())
        .orElseThrow(() -> new IllegalArgumentException("Industry not found for current tenant"));

    CrmProduct p = CrmProduct.builder()
        .tenant(t)
        .industry(industry)
        .name(name)
        .build();

    return toDto(productRepository.save(p));
  }

  public CrmProductResponse update(Long id, CrmProductDto req) {
    Tenant t = getCurrentTenant();
    CrmProduct p = productRepository.findByIdAndTenantId(id, t.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

    CrmIndustry industry = industryRepository.findByIdAndTenantId(req.getIndustryId(), t.getId())
        .orElseThrow(() -> new IllegalArgumentException("Industry not found for current tenant"));

    String newName = req.getName().trim();
    if (!p.getName().equalsIgnoreCase(newName)
        && productRepository.existsByTenantIdAndNameIgnoreCase(t.getId(), newName)) {
      throw new IllegalArgumentException("Product name already exists");
    }

    p.setIndustry(industry);
    p.setName(newName);
    return toDto(productRepository.save(p));
  }

  public void delete(Long id) {
    Tenant t = getCurrentTenant();
    CrmProduct p = productRepository.findByIdAndTenantId(id, t.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    productRepository.delete(p);
  }

  private CrmProductResponse toDto(CrmProduct product) {
    return CrmProductResponse.builder()
        .id(product.getId())
        .name(product.getName())
        .industryId(product.getIndustry().getId())
        .industryName(product.getIndustry().getName())
        .createdAt(product.getCreatedAt())
        .updatedAt(product.getUpdatedAt())
        .build();
  }
}
