package com.example.multi_tanent.tenant.payroll.dto;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

import com.example.multi_tanent.tenant.base.entity.CompanyInfo;

@Data
public class CompanyInfoResponse {
    private Long id;
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
    private List<CompanyLocationResponse> locations;
    private List<CompanyBankAccountResponse> bankAccounts;

    public static CompanyInfoResponse fromEntity(CompanyInfo entity) {
        if (entity == null) return null;
        CompanyInfoResponse dto = new CompanyInfoResponse();
        dto.setId(entity.getId());
        dto.setCompanyName(entity.getCompanyName());
        dto.setAddress(entity.getAddress());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setPostalCode(entity.getPostalCode());
        dto.setCountry(entity.getCountry());
        dto.setPhone(entity.getPhone());
        dto.setEmail(entity.getEmail());
        dto.setWebsite(entity.getWebsite());
        dto.setPan(entity.getPan());
        dto.setTan(entity.getTan());
        dto.setGstIn(entity.getGstIn());
        dto.setPfRegistrationNumber(entity.getPfRegistrationNumber());
        dto.setEsiRegistrationNumber(entity.getEsiRegistrationNumber());

        if (entity.getLocations() != null) {
            dto.setLocations(entity.getLocations().stream()
                    .map(CompanyLocationResponse::fromEntity)
                    .collect(Collectors.toList()));
        }

        if (entity.getBankAccounts() != null) {
            dto.setBankAccounts(entity.getBankAccounts().stream()
                    .map(CompanyBankAccountResponse::fromEntity)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}