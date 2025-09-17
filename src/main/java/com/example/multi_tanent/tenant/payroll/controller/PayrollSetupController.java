package com.example.multi_tanent.tenant.payroll.controller;

import com.example.multi_tanent.tenant.payroll.dto.*;
import com.example.multi_tanent.tenant.payroll.entity.*;
import com.example.multi_tanent.tenant.payroll.service.PayrollSetupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll-setup")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('TENANT_ADMIN','HR')")
public class PayrollSetupController {

    private final PayrollSetupService setupService;

    public PayrollSetupController(PayrollSetupService setupService) {
        this.setupService = setupService;
    }

    // Payroll Setting
    @GetMapping("/settings")
    public ResponseEntity<PayrollSetting> getPayrollSetting() {
        return ResponseEntity.ok(setupService.getPayrollSetting());
    }

    @PostMapping("/settings")
    public ResponseEntity<PayrollSetting> createOrUpdatePayrollSetting(@RequestBody PayrollSettingRequest request) {
        return ResponseEntity.ok(setupService.createOrUpdatePayrollSetting(request));
    }

    // Salary Components
    @GetMapping("/salary-components")
    public ResponseEntity<List<SalaryComponent>> getAllSalaryComponents() {
        return ResponseEntity.ok(setupService.getAllSalaryComponents());
    }

    @PostMapping("/salary-components")
    public ResponseEntity<SalaryComponent> createSalaryComponent(@RequestBody SalaryComponentRequest request) {
        return ResponseEntity.ok(setupService.createSalaryComponent(request));
    }

    // Statutory Rules
    @GetMapping("/statutory-rules")
    public ResponseEntity<List<StatutoryRule>> getAllStatutoryRules() {
        return ResponseEntity.ok(setupService.getAllStatutoryRules());
    }

    @PostMapping("/statutory-rules")
    public ResponseEntity<StatutoryRule> createStatutoryRule(@RequestBody StatutoryRuleRequest request) {
        return ResponseEntity.ok(setupService.createStatutoryRule(request));
    }

    // Loan Products
    @GetMapping("/loan-products")
    public ResponseEntity<List<LoanProduct>> getAllLoanProducts() {
        return ResponseEntity.ok(setupService.getAllLoanProducts());
    }

    @PostMapping("/loan-products")
    public ResponseEntity<LoanProduct> createLoanProduct(@RequestBody LoanProductRequest request) {
        return ResponseEntity.ok(setupService.createLoanProduct(request));
    }

    // TODO: Add endpoints for Employee Bank Account

}