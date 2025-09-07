package com.example.multi_tanent.tenant.controller;

import java.net.URI;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.multi_tanent.tenant.entity.BankDetails;
import com.example.multi_tanent.tenant.repository.BankDetailRepository;
import com.example.multi_tanent.tenant.repository.EmployeeRepository;
import com.example.multi_tanent.tenant.repository.SalaryDetailsRepository;
import com.example.multi_tanent.tenant.tenantDto.BankDetailsRequest;

@RestController
@RequestMapping("/api/bankdetails")
@CrossOrigin(origins = "*")
public class BankDetailsController {
    private final EmployeeRepository employeeRepository;
    private final BankDetailRepository bankDetailRepository;

    public BankDetailsController(EmployeeRepository employeeRepository, SalaryDetailsRepository salaryDetailsRepository, BankDetailRepository bankDetailRepository) {
        this.employeeRepository = employeeRepository;
        this.bankDetailRepository = bankDetailRepository;
    }

    @PostMapping("/{employeeCode}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
    public ResponseEntity<BankDetails> registerBankDetails(@PathVariable String employeeCode, @RequestBody BankDetailsRequest bankDetailsRequest) {
        return employeeRepository.findByEmployeeCode(employeeCode)
                // Use the already-loaded SalaryDetails from the entity graph to avoid an extra query
                .flatMap(employee -> employee.getSalaryDetails().stream().findFirst())
                .map(salaryDetails -> {
                    BankDetails bankDetails = new BankDetails();
                    bankDetails.setSalaryDetails(salaryDetails);
                    bankDetails.setAccountHolderName(bankDetailsRequest.getAccountHolderName());
                    bankDetails.setAccountNumber(bankDetailsRequest.getAccountNumber());
                    bankDetails.setIfscOrSwift(bankDetailsRequest.getIfscOrSwift());
                    bankDetails.setBankName(bankDetailsRequest.getBankName());
                    bankDetails.setBranch(bankDetailsRequest.getBranch());
                    bankDetails.setPayoutActive(bankDetailsRequest.getPayoutActive());
                    BankDetails savedBankDetails = bankDetailRepository.save(bankDetails);

                    URI location = ServletUriComponentsBuilder
                            .fromCurrentContextPath().path("/api/bankdetails/{id}")
                            .buildAndExpand(savedBankDetails.getId()).toUri();

                    return ResponseEntity.created(location).body(savedBankDetails);
                })
                .orElse(ResponseEntity.notFound().build());
    }

   @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
    public ResponseEntity<BankDetails> updateBankDetails(@PathVariable Long id, @RequestBody BankDetailsRequest bankDetailsRequest) {
        return bankDetailRepository.findById(id)
                .map(bankDetails -> {
                    bankDetails.setAccountHolderName(bankDetailsRequest.getAccountHolderName());
                    bankDetails.setAccountNumber(bankDetailsRequest.getAccountNumber());
                    bankDetails.setIfscOrSwift(bankDetailsRequest.getIfscOrSwift());
                    bankDetails.setBankName(bankDetailsRequest.getBankName());
                    bankDetails.setBranch(bankDetailsRequest.getBranch());
                    bankDetails.setPayoutActive(bankDetailsRequest.getPayoutActive());
                    BankDetails updatedBankDetails = bankDetailRepository.save(bankDetails);
                    return ResponseEntity.ok(updatedBankDetails);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    

    @GetMapping("/{employeeCode}")
    public ResponseEntity<Set<BankDetails>> getBankDetails(@PathVariable String employeeCode){
        return employeeRepository.findByEmployeeCode(employeeCode)
                // Use the already-loaded SalaryDetails from the entity graph to avoid LazyInitializationException
                .flatMap(employee -> employee.getSalaryDetails().stream().findFirst())
                .map(salaryDetails -> ResponseEntity.ok(salaryDetails.getBankDetails()))
                .orElse(ResponseEntity.notFound().build());
    }
    
}
