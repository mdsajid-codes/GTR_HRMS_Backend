package com.example.multi_tanent.tenant.controller;

import java.net.URI;
import org.springframework.http.HttpStatus;
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

import com.example.multi_tanent.tenant.entity.SalaryDetails;
import com.example.multi_tanent.tenant.repository.EmployeeRepository;
import com.example.multi_tanent.tenant.repository.SalaryDetailsRepository;
import com.example.multi_tanent.tenant.tenantDto.SalaryDetailRequest;

@RestController
@RequestMapping("/api/salaryDetails")
@CrossOrigin(origins = "*")
public class SalaryDetailController {
    private final EmployeeRepository employeeRepository;
    private final SalaryDetailsRepository salaryDetailsRepository;    

    public SalaryDetailController(EmployeeRepository employeeRepository, SalaryDetailsRepository salaryDetailsRepository){
        this.employeeRepository = employeeRepository;
        this.salaryDetailsRepository = salaryDetailsRepository;
    }

    @PostMapping("/{employeeCode}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
    public ResponseEntity<?> registerSalaryDetails(@PathVariable String employeeCode, @RequestBody SalaryDetailRequest salaryDetailRequest){
        return employeeRepository.findByEmployeeCode(employeeCode)
                .map(employee -> {
                    if(salaryDetailsRepository.findByEmployeeId(employee.getId()).isPresent()){
                        return ResponseEntity.status(HttpStatus.CONFLICT).body("Salary details for this employee already exist.");
                    }

                    SalaryDetails salaryDetails = new SalaryDetails();
                    salaryDetails.setEmployee(employee);
                    salaryDetails.setPayFrequency(salaryDetailRequest.getPayFrequency());
                    salaryDetails.setCtcAnnual(salaryDetailRequest.getCtcAnnual());
                    salaryDetails.setBonusEligible(salaryDetailRequest.getBonusEligible());
                    salaryDetails.setBonusTargetPct(salaryDetailRequest.getBonusTargetPct());
                    salaryDetails.setCurrency(salaryDetailRequest.getCurrency());

                    SalaryDetails savedSalaryDetails = salaryDetailsRepository.save(salaryDetails);

                    URI location = ServletUriComponentsBuilder
                            .fromCurrentContextPath().path("/api/salaryDetails/{employeeCode}")
                            .buildAndExpand(employeeCode).toUri();

                    return ResponseEntity.created(location).body(savedSalaryDetails);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{employeeCode}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
    public ResponseEntity<SalaryDetails> updateSalaryDetails(@PathVariable String employeeCode, @RequestBody SalaryDetailRequest salaryDetailRequest){
        return employeeRepository.findByEmployeeCode(employeeCode)
                // Use the already-loaded SalaryDetails from the entity graph to avoid LazyInitializationException
                .flatMap(employee -> employee.getSalaryDetails().stream().findFirst())
                .map(salaryDetails -> {
                    salaryDetails.setPayFrequency(salaryDetailRequest.getPayFrequency());
                    salaryDetails.setCtcAnnual(salaryDetailRequest.getCtcAnnual());
                    salaryDetails.setBonusEligible(salaryDetailRequest.getBonusEligible());
                    salaryDetails.setBonusTargetPct(salaryDetailRequest.getBonusTargetPct());
                    salaryDetails.setCurrency(salaryDetailRequest.getCurrency());
                    SalaryDetails updatedSalaryDetails = salaryDetailsRepository.save(salaryDetails);
                    return ResponseEntity.ok(updatedSalaryDetails);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{employeeCode}")
    public ResponseEntity<SalaryDetails> getSalaryDetails(@PathVariable String employeeCode){
        return employeeRepository.findByEmployeeCode(employeeCode)
                // Use the already-loaded SalaryDetails from the entity graph to avoid LazyInitializationException
                .flatMap(employee -> employee.getSalaryDetails().stream().findFirst())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
}
