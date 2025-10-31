package com.example.multi_tanent.crm.dto;

import com.example.multi_tanent.crm.entity.Address;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
@Builder
public class CrmLeadResponse {
    private Long id;
    private String firstName;
    private String lastName;

    private Long companyId;
    private String companyName;

    private String industry;
    private String designation;
    private String phone;
    private String email;
    private String website;
    private Set<String> products;
    private String requirements;
    private String leadSource;
    private Address address;
    private String notes;

    private Long ownerId;
    private String ownerName;

    private Long currentStageId;
    private String currentStageName;

    private Long locationId;
    private String locationName;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}