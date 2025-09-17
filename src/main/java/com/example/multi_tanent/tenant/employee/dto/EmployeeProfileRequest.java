package com.example.multi_tanent.tenant.employee.dto;

import lombok.Data;

@Data
public class EmployeeProfileRequest {
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String emergencyContactName;
    private String emergencyContactRelation;
    private String emergencyContactPhone;
    private String bankName;
    private String bankAccountNumber;
    private String ifscCode;
    private String bloodGroup;
    private String notes;
}