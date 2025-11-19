package com.example.multi_tanent.sales.service;

import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.production.repository.ProCategoryRepository;
import com.example.multi_tanent.production.repository.ProSubCategoryRepository;
import com.example.multi_tanent.sales.dto.SaleProductRequest;
import com.example.multi_tanent.sales.dto.SaleProductResponse;
import com.example.multi_tanent.sales.entity.SaleProduct;
import com.example.multi_tanent.sales.repository.SaleProductRepository;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import com.example.multi_tanent.spersusers.repository.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional("tenantTx")
public class SaleProductService {

    private final SaleProductRepository productRepo;
    private final ProCategoryRepository categoryRepo;
    private final ProSubCategoryRepository subCategoryRepo;
    private final TenantRepository tenantRepo;

    private Tenant getCurrentTenant() {
        String tenantId = TenantContext.getTenantId();
        return tenantRepo.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalStateException("Tenant not found: " + tenantId));
    }
  
    public SaleProductResponse create(SaleProductRequest req) {
        Tenant tenant = getCurrentTenant();
        if (productRepo.existsByTenantIdAndSkuIgnoreCase(tenant.getId(), req.getSku())) {
            throw new IllegalArgumentException("Product with SKU '" + req.getSku() + "' already exists.");
        }

        SaleProduct product = new SaleProduct();
        applyRequestToEntity(req, product);

        return toResponse(productRepo.save(product));
    }

    public SaleProductResponse update(Long id, SaleProductRequest req) {
        Tenant tenant = getCurrentTenant();
        SaleProduct product = productRepo.findByTenantIdAndId(tenant.getId(), id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));

        if (!product.getSku().equalsIgnoreCase(req.getSku()) && productRepo.existsByTenantIdAndSkuIgnoreCase(tenant.getId(), req.getSku())) {
            throw new IllegalArgumentException("Product with SKU '" + req.getSku() + "' already exists.");
        }

        applyRequestToEntity(req, product);
        return toResponse(productRepo.save(product));
    }

    @Transactional(readOnly = true)
    public SaleProductResponse getById(Long id) {
        Long tenantId = getCurrentTenant().getId();
        return productRepo.findByTenantIdAndId(tenantId, id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<SaleProductResponse> getAll(Pageable pageable) {
        Long tenantId = getCurrentTenant().getId();
        return productRepo.findByTenantId(tenantId, pageable).map(this::toResponse);
    }

    public void delete(Long id) {
        Long tenantId = getCurrentTenant().getId();
        SaleProduct product = productRepo.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
        productRepo.delete(product);
    }

    private void applyRequestToEntity(SaleProductRequest req, SaleProduct entity) {
        entity.setTenant(getCurrentTenant());
        entity.setSku(req.getSku());
        entity.setName(req.getName());
        entity.setDescription(req.getDescription());
        entity.setUom(req.getUom());
        entity.setTaxRate(req.getTaxRate());
        entity.setUnitPrice(req.getUnitPrice());
        entity.setStatus(req.getStatus());

        if (req.getCategoryId() != null) {
            entity.setCategory(categoryRepo.findById(req.getCategoryId()).orElse(null));
        } else {
            entity.setCategory(null);
        }

        if (req.getSubCategoryId() != null) {
            entity.setSubCategory(subCategoryRepo.findById(req.getSubCategoryId()).orElse(null));
        } else {
            entity.setSubCategory(null);
        }
    }

    private SaleProductResponse toResponse(SaleProduct entity) {
        return SaleProductResponse.builder()
                .id(entity.getId())
                .sku(entity.getSku())
                .name(entity.getName())
                .description(entity.getDescription())
                .uom(entity.getUom())
                .taxRate(entity.getTaxRate())
                .unitPrice(entity.getUnitPrice())
                .status(entity.getStatus())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .subCategoryId(entity.getSubCategory() != null ? entity.getSubCategory().getId() : null)
                .subCategoryName(entity.getSubCategory() != null ? entity.getSubCategory().getName() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
