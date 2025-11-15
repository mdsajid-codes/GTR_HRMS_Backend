package com.example.multi_tanent.sales.service;

import com.example.multi_tanent.sales.dto.SaleProductRequest;
import com.example.multi_tanent.sales.dto.SaleProductResponse;
import com.example.multi_tanent.sales.entity.SaleProduct;
import com.example.multi_tanent.sales.repository.SaleProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SaleProductService {

    private final SaleProductRepository productRepo;
  
    public SaleProductResponse create(SaleProductRequest req) {
        if (productRepo.existsBySku(req.getSku())) {
            throw new IllegalArgumentException("Product with SKU '" + req.getSku() + "' already exists.");
        }

        SaleProduct product = new SaleProduct();
        applyRequestToEntity(req, product);

        return toResponse(productRepo.save(product));
    }

    public SaleProductResponse update(Long id, SaleProductRequest req) {
        SaleProduct product = productRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));

        if (!product.getSku().equals(req.getSku()) && productRepo.existsBySku(req.getSku())) {
            throw new IllegalArgumentException("Product with SKU '" + req.getSku() + "' already exists.");
        }

        applyRequestToEntity(req, product);
        return toResponse(productRepo.save(product));
    }

    @Transactional(readOnly = true)
    public SaleProductResponse getById(Long id) {
        return productRepo.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<SaleProductResponse> getAll() {
        return productRepo.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        SaleProduct product = productRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
        productRepo.delete(product);
    }

    private void applyRequestToEntity(SaleProductRequest req, SaleProduct entity) {
        entity.setSku(req.getSku());
        entity.setName(req.getName());
        entity.setDescription(req.getDescription());
        entity.setUom(req.getUom());
        entity.setTaxRate(req.getTaxRate());
        entity.setUnitPrice(req.getUnitPrice());
        entity.setStatus(req.getStatus());
    }

    private SaleProductResponse toResponse(SaleProduct entity) {
        return SaleProductResponse.builder()
                .id(entity.getId()).sku(entity.getSku()).name(entity.getName())
                .description(entity.getDescription()).uom(entity.getUom())
                .taxRate(entity.getTaxRate()).unitPrice(entity.getUnitPrice())
                .status(entity.getStatus()).createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt()).build();
    }
}
