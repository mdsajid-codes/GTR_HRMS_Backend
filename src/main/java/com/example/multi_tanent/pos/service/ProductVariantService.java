package com.example.multi_tanent.pos.service;

import com.example.multi_tanent.pos.dto.ProductVariantDto;
import com.example.multi_tanent.pos.dto.ProductVariantRequest;
import com.example.multi_tanent.pos.entity.*;
import com.example.multi_tanent.pos.repository.ProductRepository;
import com.example.multi_tanent.pos.repository.ProductVariantRepository;
import com.example.multi_tanent.pos.repository.SaleItemRepository;
import com.example.multi_tanent.pos.repository.TaxRateRepository;
import com.example.multi_tanent.pos.repository.TenantRepository;
import com.example.multi_tanent.spersusers.enitity.Tenant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional("tenantTx")
public class ProductVariantService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final TenantRepository tenantRepository;
    private final TaxRateRepository taxRateRepository;
    private final SaleItemRepository saleItemRepository;

    public ProductVariantService(ProductRepository productRepository,
                                 ProductVariantRepository productVariantRepository,
                                 TenantRepository tenantRepository,
                                 TaxRateRepository taxRateRepository,
                                 SaleItemRepository saleItemRepository) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.tenantRepository = tenantRepository;
        this.taxRateRepository = taxRateRepository;
        this.saleItemRepository = saleItemRepository;
    }

    private Tenant getCurrentTenant() {
        return tenantRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Tenant context not found. Cannot perform variant operations."));
    }

    private Product getProductForCurrentTenant(Long productId) {
        Tenant currentTenant = getCurrentTenant();
        return productRepository.findByIdAndTenantId(productId, currentTenant.getId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
    }

    public ProductVariantDto addVariant(Long productId, ProductVariantRequest request) {
        Product product = getProductForCurrentTenant(productId);
        ProductVariant variant = mapRequestToEntity(request, new ProductVariant(), product);
        ProductVariant savedVariant = productVariantRepository.save(variant);
        return toDto(savedVariant);
    }

    @Transactional(readOnly = true)
    public List<ProductVariantDto> getVariantsForProduct(Long productId) {
        Product product = getProductForCurrentTenant(productId);
        return product.getVariants().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ProductVariantDto> getVariantById(Long productId, Long variantId) {
        getProductForCurrentTenant(productId); // Ensures product exists for the tenant
        return productVariantRepository.findByIdAndProductId(variantId, productId).map(this::toDto);
    }

    public ProductVariantDto updateVariant(Long productId, Long variantId, ProductVariantRequest request) {
        Product product = getProductForCurrentTenant(productId);
        ProductVariant existingVariant = productVariantRepository.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new RuntimeException("ProductVariant not found with id: " + variantId + " for product " + productId));

        ProductVariant updatedVariant = mapRequestToEntity(request, existingVariant, product);
        return toDto(productVariantRepository.save(updatedVariant));
    }

    public void deleteVariant(Long productId, Long variantId) {
        Product product = getProductForCurrentTenant(productId); // Ensure product exists for the tenant
        ProductVariant variant = productVariantRepository.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new RuntimeException("ProductVariant not found with id: " + variantId));

        boolean hasBeenSold = saleItemRepository.existsByProductVariantId(variant.getId());
        if (hasBeenSold) {
            variant.setActive(false); // Soft delete by marking as inactive
            productVariantRepository.save(variant);
        } else {
            productVariantRepository.delete(variant); // Hard delete if it has never been sold
        }
    }

    private ProductVariant mapRequestToEntity(ProductVariantRequest request, ProductVariant variant, Product product) {
        variant.setProduct(product);
        variant.setSku(request.getSku());
        variant.setBarcode(request.getBarcode());
        variant.setAttributes(request.getAttributes());
        variant.setPriceCents(request.getPriceCents());

        // If active flag is not provided in request, default to true for new/updated variants
        if (request.getActive() != null) {
            variant.setActive(request.getActive());
        } else if (variant.getId() == null) { // New variant
            variant.setActive(true);
        }
        variant.setCostCents(request.getCostCents());
        variant.setImageUrl(request.getImageUrl());

        if (request.getTaxRateId() != null) {
            TaxRate taxRate = taxRateRepository.findByIdAndTenantId(request.getTaxRateId(), product.getTenant().getId())
                    .orElseThrow(() -> new RuntimeException("TaxRate not found with id: " + request.getTaxRateId()));
            variant.setTaxRate(taxRate);
        } else {
            variant.setTaxRate(null);
        }
        return variant;
    }

    ProductVariantDto toDto(ProductVariant variant) {
        ProductVariantDto dto = new ProductVariantDto();
        dto.setId(variant.getId());
        dto.setSku(variant.getSku());
        dto.setBarcode(variant.getBarcode());
        dto.setAttributes(variant.getAttributes());
        dto.setPriceCents(variant.getPriceCents());
        dto.setActive(variant.isActive());
        dto.setCostCents(variant.getCostCents());
        if (variant.getImageUrl() != null && !variant.getImageUrl().isBlank()) {
            dto.setImageUrl(buildImageUrl(variant.getImageUrl()));
        }
        if (variant.getTaxRate() != null) {
            dto.setTaxRateId(variant.getTaxRate().getId());
            dto.setTaxRateName(variant.getTaxRate().getName());
            dto.setTaxRatePercent(variant.getTaxRate().getPercent());
        }
        return dto;
    }

    private String buildImageUrl(String relativePath) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/pos/uploads/view/")
                .path(relativePath)
                .build()
                .toUriString()
                .replace("\\", "/"); // Ensure forward slashes for URL
    }
}