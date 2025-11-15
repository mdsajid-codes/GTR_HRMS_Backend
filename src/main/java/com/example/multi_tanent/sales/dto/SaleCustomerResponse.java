package com.example.multi_tanent.sales.dto;

import com.example.multi_tanent.sales.entity.SalesAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleCustomerResponse {
    private Long id;
    private String code;
    private String name;
    private String email;
    private String phone;
    private SalesAddress billingAddress;
    private SalesAddress shippingAddress;
    private String gstOrVatNumber;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
