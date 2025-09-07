package com.example.multi_tanent.tenant.controller;

import java.net.URI;
import java.util.Set;

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

import com.example.multi_tanent.tenant.entity.CompensationComponents;
import com.example.multi_tanent.tenant.repository.CompensationRepository;
import com.example.multi_tanent.tenant.repository.EmployeeRepository;
import com.example.multi_tanent.tenant.repository.SalaryDetailsRepository;
import com.example.multi_tanent.tenant.tenantDto.CompensationRequest;

@RestController
@RequestMapping("/api/compensations")
@CrossOrigin (origins = "*")
public class CompensationController {
    private final EmployeeRepository employeeRepository;
    private final SalaryDetailsRepository salaryDetailsRepository;
    private final CompensationRepository compensationRepository;

    public CompensationController(EmployeeRepository employeeRepository, SalaryDetailsRepository salaryDetailsRepository, CompensationRepository compensationRepository){
        this.employeeRepository = employeeRepository;
        this.salaryDetailsRepository = salaryDetailsRepository;
        this.compensationRepository = compensationRepository;
    }

    @PostMapping("/{employeeCode}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
    public ResponseEntity<?> registerCompensation(@PathVariable String employeeCode, @RequestBody CompensationRequest compensationRequest){
        return employeeRepository.findByEmployeeCode(employeeCode)
                .flatMap(employee -> salaryDetailsRepository.findByEmployeeId(employee.getId()))
                .map(salaryDetails -> {
                    // Prevent creating a duplicate component type for the same employee salary details
                    if (compensationRepository.findBySalaryDetailsIdAndComponentType(salaryDetails.getId(), compensationRequest.getComponentType()).isPresent()) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body("Compensation component of type '" + compensationRequest.getComponentType() + "' already exists for this employee.");
                    }

                    CompensationComponents compensationComponents = new CompensationComponents();
                    compensationComponents.setSalaryDetails(salaryDetails);
                    compensationComponents.setComponentType(compensationRequest.getComponentType());
                    compensationComponents.setAmount(compensationRequest.getAmount());
                    compensationComponents.setPercentOfBasic(compensationRequest.getPercentOfBasic());
                    compensationComponents.setTaxTreatment(compensationRequest.getTaxTreatment());

                    CompensationComponents savedCompensation = compensationRepository.save(compensationComponents);

                    URI location = ServletUriComponentsBuilder
                            .fromCurrentContextPath().path("/api/compensations/{id}")
                            .buildAndExpand(savedCompensation.getId()).toUri();

                    return ResponseEntity.created(location).body(savedCompensation);
                })
                .orElse(ResponseEntity.notFound().build());
                
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
    public ResponseEntity<CompensationComponents> updateCompensation(@PathVariable Long id, @RequestBody CompensationRequest compensationRequest){
        return compensationRepository.findById(id)
                .map(compensationComponents -> {
                    // Component type is part of the business key and should not be changed via update.
                    // If a change is needed, it should be a delete-and-create operation.
                    compensationComponents.setAmount(compensationRequest.getAmount());
                    compensationComponents.setPercentOfBasic(compensationRequest.getPercentOfBasic());
                    compensationComponents.setTaxTreatment(compensationRequest.getTaxTreatment());

                    CompensationComponents updatedCompensation = compensationRepository.save(compensationComponents);
                    return ResponseEntity.ok(updatedCompensation);
                })
                .orElse(ResponseEntity.notFound().build());
                
    }

    @GetMapping("/{employeeCode}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
    public ResponseEntity<Set<CompensationComponents>> getCompensation(@PathVariable String employeeCode){
        return employeeRepository.findByEmployeeCode(employeeCode)
                .flatMap(employee -> salaryDetailsRepository.findByEmployeeId(employee.getId()))
                .map(salaryDetails -> ResponseEntity.ok(salaryDetails.getCompensationComponents()))
                .orElse(ResponseEntity.notFound().build());
    }
}
