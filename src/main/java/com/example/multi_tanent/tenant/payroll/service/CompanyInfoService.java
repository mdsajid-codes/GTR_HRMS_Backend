package com.example.multi_tanent.tenant.payroll.service;

import com.example.multi_tanent.tenant.base.entity.CompanyInfo;
import com.example.multi_tanent.tenant.payroll.dto.CompanyInfoRequest;
import com.example.multi_tanent.tenant.payroll.repository.CompanyInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(transactionManager = "tenantTx")
public class CompanyInfoService {

    private final CompanyInfoRepository companyInfoRepository;

    public CompanyInfoService(CompanyInfoRepository companyInfoRepository) {
        this.companyInfoRepository = companyInfoRepository;
    }

    /**
     * Retrieves the company information for the current tenant.
     * As this is a singleton per tenant, it fetches the first available record.
     */
    public CompanyInfo getCompanyInfo() {
        // Find the first CompanyInfo record.
        CompanyInfo companyInfo = companyInfoRepository.findAll().stream().findFirst().orElse(null);
        if (companyInfo != null) {
            // Explicitly initialize lazy-loaded collections within the transaction to prevent LazyInitializationException.
            companyInfo.getLocations().size();
            companyInfo.getBankAccounts().size();
        }
        return companyInfo;
    }

    /**
     * Creates a new CompanyInfo record or updates the existing one.
     */
    public CompanyInfo createOrUpdateCompanyInfo(CompanyInfoRequest request) {
        CompanyInfo companyInfo = getCompanyInfo();
        if (companyInfo == null) {
            companyInfo = new CompanyInfo();
        }
        mapCompanyInfoRequestToEntity(request, companyInfo);
        return companyInfoRepository.save(companyInfo);
    }

    private void mapCompanyInfoRequestToEntity(CompanyInfoRequest request, CompanyInfo entity) {
        entity.setCompanyName(request.getCompanyName());
        entity.setAddress(request.getAddress());
        entity.setCity(request.getCity());
        entity.setState(request.getState());
        entity.setPostalCode(request.getPostalCode());
        entity.setCountry(request.getCountry());
        entity.setPhone(request.getPhone());
        entity.setEmail(request.getEmail());
        entity.setWebsite(request.getWebsite());
        entity.setPan(request.getPan());
        entity.setTan(request.getTan());
        entity.setGstIn(request.getGstIn());
        entity.setPfRegistrationNumber(request.getPfRegistrationNumber());
        entity.setEsiRegistrationNumber(request.getEsiRegistrationNumber());

    }
}