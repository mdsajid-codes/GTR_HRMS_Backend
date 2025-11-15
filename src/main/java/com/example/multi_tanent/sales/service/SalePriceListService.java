package com.example.multi_tanent.sales.service;


import com.example.multi_tanent.sales.dto.SalePriceListItemRequest;
import com.example.multi_tanent.sales.dto.SalePriceListItemResponse;
import com.example.multi_tanent.sales.dto.SalePriceListRequest;
import com.example.multi_tanent.sales.dto.SalePriceListResponse;
import com.example.multi_tanent.sales.entity.SalePriceList;
import com.example.multi_tanent.sales.entity.SalePriceListItem;

import com.example.multi_tanent.sales.repository.SalePriceListItemRepository;
import com.example.multi_tanent.sales.repository.SalePriceListRepository;
import com.example.multi_tanent.sales.repository.SaleProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SalePriceListService {

    private final SalePriceListRepository priceListRepo;
    private final SalePriceListItemRepository priceListItemRepo;
    private final SalePriceListItemService priceListItemService;

    public SalePriceListResponse create(SalePriceListRequest req) {
        if (priceListRepo.existsByName(req.getName())) {
            throw new IllegalArgumentException("Price List with name '" + req.getName() + "' already exists.");
        }

        SalePriceList priceList = new SalePriceList();
        applyRequestToEntity(req, priceList);

        SalePriceList savedPriceList = priceListRepo.save(priceList);

        if (savedPriceList.isDefault()) {
            priceListRepo.unsetAllDefaults(savedPriceList.getId());
        }

        // Create the items after the price list is saved
        if (req.getItems() != null) {
            for (SalePriceListItemRequest itemRequest : req.getItems()) {
                priceListItemService.addItem(savedPriceList.getId(), itemRequest);
            }
        }

        return toResponse(savedPriceList);
    }

    public SalePriceListResponse update(Long id, SalePriceListRequest req) {
        SalePriceList priceList = priceListRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Price List not found: " + id));

        if (!priceList.getName().equals(req.getName()) && priceListRepo.existsByName(req.getName())) {
            throw new IllegalArgumentException("Price List with name '" + req.getName() + "' already exists.");
        }

        applyRequestToEntity(req, priceList);
        SalePriceList savedPriceList = priceListRepo.save(priceList);

        if (savedPriceList.isDefault()) {
            priceListRepo.unsetAllDefaults(savedPriceList.getId());
        }

        return toResponse(savedPriceList);
    }

    @Transactional(readOnly = true)
    public SalePriceListResponse getById(Long id) {
        return priceListRepo.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Price List not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<SalePriceListResponse> getAll() {
        return priceListRepo.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        SalePriceList priceList = priceListRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Price List not found: " + id));
        priceListItemRepo.deleteByPriceListId(id);
        priceListRepo.delete(priceList);
    }

    private void applyRequestToEntity(SalePriceListRequest req, SalePriceList entity) {
        entity.setName(req.getName());
        entity.setCurrency(req.getCurrency());
        entity.setDefault(req.isDefault());
    }

    private SalePriceListResponse toResponse(SalePriceList e) {
        // This is a simplified mapping. For performance, you might fetch items in a separate query
        // or use a DTO projection in the repository if lists become very large.
        List<SalePriceListItem> items = priceListItemRepo.findByPriceListId(e.getId());
        List<SalePriceListItemResponse> itemResponses = items.stream().map(this::toItemResponse).collect(Collectors.toList());

        return SalePriceListResponse.builder().id(e.getId()).name(e.getName()).currency(e.getCurrency()).isDefault(e.isDefault()).items(itemResponses).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).version(e.getVersion()).build();
    }

    private SalePriceListItemResponse toItemResponse(SalePriceListItem i) {
        return SalePriceListItemResponse.builder().id(i.getId()).productId(i.getProduct().getId()).productName(i.getProduct().getName()).productSku(i.getProduct().getSku()).unitPrice(i.getUnitPrice()).validFrom(i.getValidFrom()).validTo(i.getValidTo()).build();
    }
}
