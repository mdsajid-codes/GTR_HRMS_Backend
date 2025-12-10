package com.example.multi_tanent.production.dto;

import com.example.multi_tanent.production.enums.InventoryType;
import com.example.multi_tanent.production.enums.ItemType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProSemiFinishedRequest {
    @NotBlank(message = "Item code is required.")
    private String itemCode;

    @NotBlank(message = "Name is required.")
    private String name;

    private Long locationId;
    private InventoryType inventoryType;
    private ItemType itemType;
    private boolean forPurchase;
    private boolean forSales;
    private boolean isRoll;
    private boolean isScrapItem;
    private Long categoryId;
    private Long subCategoryId;
    private String description;
    private Long issueUnitId;
    private Long purchaseUnitId;
    private BigDecimal unitRelation;
    private BigDecimal wastagePercentage;
    private BigDecimal reorderLimit;
    private Long taxId;
    private boolean isTaxInclusive;
    private BigDecimal purchasePrice;
    private BigDecimal salesPrice;
    private String picturePath;
}
