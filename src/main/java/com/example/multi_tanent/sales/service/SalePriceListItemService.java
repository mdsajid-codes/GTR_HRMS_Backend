package com.example.multi_tanent.sales.service;

import com.example.multi_tanent.sales.dto.SalePriceListItemRequest;
import com.example.multi_tanent.sales.dto.SalePriceListItemResponse;
import com.example.multi_tanent.sales.entity.SalePriceList;
import com.example.multi_tanent.sales.entity.SalePriceListItem;
import com.example.multi_tanent.sales.entity.SaleProduct;
import com.example.multi_tanent.sales.repository.SalePriceListItemRepository;
import com.example.multi_tanent.sales.repository.SalePriceListRepository;
import com.example.multi_tanent.sales.repository.SaleProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class SalePriceListItemService {

    private final SalePriceListRepository priceListRepo;
    private final SalePriceListItemRepository itemRepo;
    private final SaleProductRepository productRepo;

    public SalePriceListItemResponse addItem(Long priceListId, SalePriceListItemRequest req) {
        SalePriceList priceList = priceListRepo.findById(priceListId)
                .orElseThrow(() -> new EntityNotFoundException("Price List not found: " + priceListId));

        SalePriceListItem item = new SalePriceListItem();
        item.setPriceList(priceList);
        applyRequestToEntity(req, item);

        return toResponse(itemRepo.save(item));
    }

    public SalePriceListItemResponse updateItem(Long itemId, SalePriceListItemRequest req) {
        // Querying through the parent ensures tenant safety
        SalePriceListItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Price List Item not found: " + itemId));

        applyRequestToEntity(req, item);
        return toResponse(itemRepo.save(item));
    }

    public void deleteItem(Long itemId) {
        SalePriceListItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Price List Item not found: " + itemId));
        itemRepo.delete(item);
    }

    private void applyRequestToEntity(SalePriceListItemRequest req, SalePriceListItem entity) {
        SaleProduct product = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + req.getProductId()));

        entity.setProduct(product);
        entity.setUnitPrice(req.getUnitPrice());
        entity.setValidFrom(req.getValidFrom());
        entity.setValidTo(req.getValidTo());
    }

    private SalePriceListItemResponse toResponse(SalePriceListItem i) {
        return SalePriceListItemResponse.builder().id(i.getId()).productId(i.getProduct().getId()).productName(i.getProduct().getName()).productSku(i.getProduct().getSku()).unitPrice(i.getUnitPrice()).validFrom(i.getValidFrom()).validTo(i.getValidTo()).build();
    }
}
