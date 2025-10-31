package com.example.multi_tanent.crm.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.multi_tanent.crm.dto.CompanyTypeDto;
import com.example.multi_tanent.crm.dto.CompanyTypeRequest;
import com.example.multi_tanent.crm.services.CrmCompanyTypeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/crm/company-types")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CompanyTypeController {

    private final CrmCompanyTypeService companyTypeService;

    @GetMapping
    public ResponseEntity<List<CompanyTypeDto>> getAllCompanyTypes() {
        return ResponseEntity.ok(companyTypeService.getAllCompanyTypes());
    }

    @PostMapping
    public ResponseEntity<CompanyTypeDto> createCompanyType(@Valid @RequestBody CompanyTypeRequest request) {
        return ResponseEntity.ok(companyTypeService.createCompanyType(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyTypeDto> updateCompanyType(@PathVariable Long id,
                                                            @Valid @RequestBody CompanyTypeRequest request) {
        return ResponseEntity.ok(companyTypeService.updateCompanyType(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompanyType(@PathVariable Long id) {
        companyTypeService.deleteCompanyType(id);
        return ResponseEntity.noContent().build();
    }
}
