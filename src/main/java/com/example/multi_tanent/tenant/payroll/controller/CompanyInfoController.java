package com.example.multi_tanent.tenant.payroll.controller;

import com.example.multi_tanent.tenant.payroll.dto.CompanyInfoRequest;
import com.example.multi_tanent.tenant.payroll.dto.CompanyInfoResponse;
import com.example.multi_tanent.tenant.payroll.entity.CompanyInfo;
import com.example.multi_tanent.tenant.payroll.service.CompanyInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company-info")
@CrossOrigin(origins = "*")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
public class CompanyInfoController {

    private final CompanyInfoService companyInfoService;

    public CompanyInfoController(CompanyInfoService companyInfoService) {
        this.companyInfoService = companyInfoService;
    }

    @GetMapping
    public ResponseEntity<CompanyInfoResponse> getCompanyInfo() {
        CompanyInfo companyInfo = companyInfoService.getCompanyInfo();
        return ResponseEntity.ok(CompanyInfoResponse.fromEntity(companyInfo));
    }

    @PostMapping
    public ResponseEntity<CompanyInfoResponse> createOrUpdateCompanyInfo(@RequestBody CompanyInfoRequest request) {
        CompanyInfo companyInfo = companyInfoService.createOrUpdateCompanyInfo(request);
        return ResponseEntity.ok(CompanyInfoResponse.fromEntity(companyInfo));
    }

    @PutMapping
    public ResponseEntity<CompanyInfoResponse> updateCompanyInfo(@RequestBody CompanyInfoRequest request) {
        CompanyInfo companyInfo = companyInfoService.createOrUpdateCompanyInfo(request);
        return ResponseEntity.ok(CompanyInfoResponse.fromEntity(companyInfo));
    }
}
