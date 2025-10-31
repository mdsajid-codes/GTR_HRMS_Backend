package com.example.multi_tanent.crm.dto;

import com.example.multi_tanent.crm.entity.Address;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class CrmCompanyResponse {
    private Long id;
    private String name;

    private Long locationId;
    private String locationName;

    private Long companyTypeId;
    private String companyTypeName;

    private Long industryId;
    private String industryName;

    private String phone;
    private String email;
    private String website;

    private Address address;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}