package com.example.multi_tanent.crm.dto;

import com.example.multi_tanent.crm.entity.Address;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CrmCompanyRequest {

    @NotBlank(message = "Company name is required.")
    private String name;

    private Long locationId;
    private Long companyTypeId;
    private Long industryId;

    private String phone;

    @Email(message = "Please provide a valid email address.")
    private String email;

    private String website;
    private Address address;
}