package com.example.multi_tanent.tenant.payroll.service;

import com.example.multi_tanent.tenant.employee.entity.Employee;
import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository;
import com.example.multi_tanent.tenant.payroll.dto.*;
import com.example.multi_tanent.tenant.payroll.entity.*;
import com.example.multi_tanent.tenant.payroll.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(transactionManager = "tenantTx")
public class PayrollSetupService {

    private final PayrollSettingRepository payrollSettingRepository;
    private final SalaryComponentRepository salaryComponentRepository;
    private final StatutoryRuleRepository statutoryRuleRepository;
    private final LoanProductRepository loanProductRepository;
    private final EmployeeBankAccountRepository employeeBankAccountRepository;
    private final EmployeeRepository employeeRepository;

    public PayrollSetupService(PayrollSettingRepository payrollSettingRepository,
                               SalaryComponentRepository salaryComponentRepository,
                               StatutoryRuleRepository statutoryRuleRepository,
                               LoanProductRepository loanProductRepository,
                               EmployeeBankAccountRepository employeeBankAccountRepository,
                               EmployeeRepository employeeRepository) {
        this.payrollSettingRepository = payrollSettingRepository;
        this.salaryComponentRepository = salaryComponentRepository;
        this.statutoryRuleRepository = statutoryRuleRepository;
        this.loanProductRepository = loanProductRepository;
        this.employeeBankAccountRepository = employeeBankAccountRepository;
        this.employeeRepository = employeeRepository;
    }

    // Payroll Setting (Singleton per tenant)
    public PayrollSetting getPayrollSetting() {
        return payrollSettingRepository.findAll().stream().findFirst().orElse(null);
    }

    public PayrollSetting createOrUpdatePayrollSetting(PayrollSettingRequest request) {
        PayrollSetting setting = getPayrollSetting();
        if (setting == null) {
            setting = new PayrollSetting();
        }
        setting.setPayFrequency(request.getPayFrequency());
        setting.setPayCycleDay(request.getPayCycleDay());
        setting.setPayslipGenerationDay(request.getPayslipGenerationDay());
        setting.setIncludeHolidaysInPayslip(request.isIncludeHolidaysInPayslip());
        setting.setIncludeLeaveBalanceInPayslip(request.isIncludeLeaveBalanceInPayslip());
        return payrollSettingRepository.save(setting);
    }

    // Salary Component (CRUD)
    public List<SalaryComponent> getAllSalaryComponents() {
        return salaryComponentRepository.findAll();
    }

    public SalaryComponent createSalaryComponent(SalaryComponentRequest request) {
        salaryComponentRepository.findByCode(request.getCode()).ifPresent(s -> {
            throw new IllegalArgumentException("Salary Component with code " + request.getCode() + " already exists.");
        });
        SalaryComponent component = new SalaryComponent();
        mapSalaryComponentRequest(request, component);
        return salaryComponentRepository.save(component);
    }

    public SalaryComponent updateSalaryComponent(Long id, SalaryComponentRequest request) {
        SalaryComponent component = salaryComponentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salary Component not found with id: " + id));
        mapSalaryComponentRequest(request, component);
        return salaryComponentRepository.save(component);
    }

    // Statutory Rule (CRUD)
    public List<StatutoryRule> getAllStatutoryRules() {
        return statutoryRuleRepository.findAll();
    }

    public StatutoryRule createStatutoryRule(StatutoryRuleRequest request) {
        StatutoryRule rule = new StatutoryRule();
        mapStatutoryRuleRequest(request, rule);
        return statutoryRuleRepository.save(rule);
    }

    public StatutoryRule updateStatutoryRule(Long id, StatutoryRuleRequest request) {
        StatutoryRule rule = statutoryRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Statutory Rule not found with id: " + id));
        mapStatutoryRuleRequest(request, rule);
        return statutoryRuleRepository.save(rule);
    }

    // Loan Product (CRUD)
    public List<LoanProduct> getAllLoanProducts() {
        return loanProductRepository.findAll();
    }

    public LoanProduct createLoanProduct(LoanProductRequest request) {
        LoanProduct product = new LoanProduct();
        mapLoanProductRequest(request, product);
        return loanProductRepository.save(product);
    }

    public LoanProduct updateLoanProduct(Long id, LoanProductRequest request) {
        LoanProduct product = loanProductRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan Product not found with id: " + id));
        mapLoanProductRequest(request, product);
        return loanProductRepository.save(product);
    }

    // Employee Bank Account
    public EmployeeBankAccount getEmployeeBankAccount(String employeeCode) {
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new RuntimeException("Employee not found with code: " + employeeCode));
        return employeeBankAccountRepository.findByEmployeeId(employee.getId()).orElse(null);
    }

    public EmployeeBankAccount createOrUpdateEmployeeBankAccount(EmployeeBankAccountRequest request) {
        Employee employee = employeeRepository.findByEmployeeCode(request.getEmployeeCode())
                .orElseThrow(() -> new RuntimeException("Employee not found with code: " + request.getEmployeeCode()));
        EmployeeBankAccount account = employeeBankAccountRepository.findByEmployeeId(employee.getId())
                .orElse(new EmployeeBankAccount());
        account.setEmployee(employee);
        account.setBankName(request.getBankName());
        account.setAccountNumber(request.getAccountNumber());
        account.setIfscCode(request.getIfscCode());
        account.setAccountHolderName(request.getAccountHolderName());
        account.setPrimary(request.isPrimary());
        return employeeBankAccountRepository.save(account);
    }

    // --- Mappers ---
    private void mapSalaryComponentRequest(SalaryComponentRequest request, SalaryComponent entity) { /* ... */ }
    private void mapStatutoryRuleRequest(StatutoryRuleRequest request, StatutoryRule entity) { /* ... */ }
    private void mapLoanProductRequest(LoanProductRequest request, LoanProduct entity) { /* ... */ }
}