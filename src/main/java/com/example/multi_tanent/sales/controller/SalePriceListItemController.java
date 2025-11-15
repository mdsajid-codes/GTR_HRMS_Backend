package com.example.multi_tanent.sales.controller;

import com.example.multi_tanent.sales.dto.SalePriceListItemRequest;
import com.example.multi_tanent.sales.dto.SalePriceListItemResponse;
import com.example.multi_tanent.sales.service.SalePriceListItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales/pricelists")
@RequiredArgsConstructor
public class SalePriceListItemController {

    private final SalePriceListItemService itemService;

    @PostMapping("/{priceListId}/items")
    public ResponseEntity<SalePriceListItemResponse> addItemToPriceList(@PathVariable Long priceListId, @RequestBody SalePriceListItemRequest request) {
        return new ResponseEntity<>(itemService.addItem(priceListId, request), HttpStatus.CREATED);
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<SalePriceListItemResponse> updatePriceListItem(@PathVariable Long itemId, @RequestBody SalePriceListItemRequest request) {
        return ResponseEntity.ok(itemService.updateItem(itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deletePriceListItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }
}
