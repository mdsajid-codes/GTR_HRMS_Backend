package com.example.multi_tanent.crm.controller;

import com.example.multi_tanent.crm.dto.CrmCompanyRequest;
import com.example.multi_tanent.crm.dto.CrmCompanyResponse;
import com.example.multi_tanent.crm.services.CrmCompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crm/companies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CrmCompanyController {

    private final CrmCompanyService companyService;

    @PostMapping
    public ResponseEntity<CrmCompanyResponse> createCompany(@Valid @RequestBody CrmCompanyRequest request) {
        return new ResponseEntity<>(companyService.createCompany(request), HttpStatus.CREATED);
    }

    @GetMapping
    public List<CrmCompanyResponse> getAllCompanies() {
        return companyService.getAllCompanies();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrmCompanyResponse> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CrmCompanyResponse> updateCompany(@PathVariable Long id, @Valid @RequestBody CrmCompanyRequest request) {
        return ResponseEntity.ok(companyService.updateCompany(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }
}