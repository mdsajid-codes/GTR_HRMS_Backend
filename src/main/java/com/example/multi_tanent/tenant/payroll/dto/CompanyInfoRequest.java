package com.example.multi_tanent.tenant.payroll.dto;

import lombok.Data;

@Data
public class CompanyInfoRequest {
    private String companyName;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String phone;
    private String email;
    private String website;
    private String pan;
    private String tan;
    private String gstIn;
    private String pfRegistrationNumber;
    private String esiRegistrationNumber;
}
