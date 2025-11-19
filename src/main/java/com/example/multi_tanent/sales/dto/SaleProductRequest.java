package com.example.multi_tanent.sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SaleProductRequest {

    @NotBlank(message = "SKU is required.")
    @Size(max = 64, message = "SKU cannot exceed 64 characters.")
    private String sku;

    @NotBlank(message = "Product name is required.")
    @Size(max = 150, message = "Name cannot exceed 150 characters.")
    private String name;

    @Size(max = 512, message = "Description cannot exceed 512 characters.")
    private String description;

    @Size(max = 16, message = "UOM cannot exceed 16 characters.")
    private String uom;

    private BigDecimal taxRate;

    @NotNull(message = "Unit price is required.")
    @PositiveOrZero(message = "Unit price must be zero or positive.")
    private BigDecimal unitPrice;

    @Size(max = 20, message = "Status cannot exceed 20 characters.")
    private String status;

    private Long categoryId;

    private Long subCategoryId;
}