package com.example.multi_tanent.pos.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class SaleDto {
    private Long id;
    private String invoiceNo;
    private OffsetDateTime invoiceDate;
    private String status;
    private String paymentStatus;

    private Long customerId;
    private String customerName;

    private Long storeId;
    private String storeName;

    private Long subtotalCents;
    private Long taxCents;
    private Long discountCents;
    private Long totalCents;

    private List<SaleItemDto> items;
}