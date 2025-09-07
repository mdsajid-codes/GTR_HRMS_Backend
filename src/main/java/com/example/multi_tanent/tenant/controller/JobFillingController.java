package com.example.multi_tanent.tenant.controller;

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

import com.example.multi_tanent.tenant.entity.JobFilling;
import com.example.multi_tanent.tenant.repository.EmployeeRepository;
import com.example.multi_tanent.tenant.repository.JobFillingRepository;
import com.example.multi_tanent.tenant.tenantDto.JobFillingRequest;

@RestController
@RequestMapping("/api/jobFillings")
@CrossOrigin(origins = "*")
public class JobFillingController {
    private final EmployeeRepository employeeRepository;
    private final JobFillingRepository jobFillingRepository;

    public JobFillingController(EmployeeRepository employeeRepository, JobFillingRepository jobFillingRepository){
        this.employeeRepository = employeeRepository;
        this.jobFillingRepository = jobFillingRepository;
    }

    @PostMapping("/{employeeCode}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
    public ResponseEntity<JobFilling> registerJobFilling(@PathVariable String employeeCode, @RequestBody JobFillingRequest jobFillingRequest) {
        return (ResponseEntity<JobFilling>) employeeRepository.findByEmployeeCode(employeeCode)
                .map(employee -> {
                    if(jobFillingRepository.findByEmployeeId(employee.getId()).isPresent()){
                    return ResponseEntity.status(409).build();
                }

                JobFilling jobFilling = new JobFilling();
                jobFilling.setEmployee(employee);
                jobFilling.setHiringSource(jobFillingRequest.getHiringSource());
                jobFilling.setOfferDate(jobFillingRequest.getOfferDate());
                jobFilling.setOfferAcceptedDate(jobFillingRequest.getOfferAcceptedDate());
                jobFilling.setJoiningDate(jobFillingRequest.getJoiningDate());
                jobFilling.setBackgroundStatus(jobFillingRequest.getBackgroundStatus());

                JobFilling savedJobFilling = jobFillingRepository.save(jobFilling);
                return ResponseEntity.ok(savedJobFilling); // Return ResponseEntity<JobFilling>
            })
            .orElse(ResponseEntity.notFound().build()); // Handle case where employee is not found
    }

    @PutMapping("/{employeeCode}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
    public ResponseEntity<JobFilling> updateJobFilling(@PathVariable String employeeCode, @RequestBody JobFillingRequest jobFillingRequest){
        return (ResponseEntity<JobFilling>) employeeRepository.findByEmployeeCode(employeeCode)
                .flatMap(employee -> jobFillingRepository.findByEmployeeId(employee.getId()))
                .map(jobFillings -> {
                    jobFillings.setHiringSource(jobFillingRequest.getHiringSource());
                    jobFillings.setOfferDate(jobFillingRequest.getOfferDate());
                    jobFillings.setOfferAcceptedDate(jobFillingRequest.getOfferAcceptedDate());
                    jobFillings.setJoiningDate(jobFillingRequest.getJoiningDate());
                    jobFillings.setBackgroundStatus(jobFillingRequest.getBackgroundStatus());
                    JobFilling updatedJobFilling = jobFillingRepository.save(jobFillings);
                    return ResponseEntity.ok(updatedJobFilling);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{employeeCode}")
    public ResponseEntity<JobFilling> getJobFilling(@PathVariable String employeeCode){
        return employeeRepository.findByEmployeeCode(employeeCode)
                .flatMap(employee -> jobFillingRepository.findByEmployeeId(employee.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
