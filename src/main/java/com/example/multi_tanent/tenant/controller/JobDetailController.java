package com.example.multi_tanent.tenant.controller;

import java.net.URI;

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

import com.example.multi_tanent.tenant.entity.JobDetails;
import com.example.multi_tanent.tenant.repository.EmployeeRepository;
import com.example.multi_tanent.tenant.repository.JobDetailRepository;
import com.example.multi_tanent.tenant.tenantDto.JobDetailRequest;

@RestController
@RequestMapping("/api/jobDetails")
@CrossOrigin(origins = "*")
public class JobDetailController {
    private final EmployeeRepository employeeRepository;
    private final JobDetailRepository jobDetailRepository;

    public JobDetailController(EmployeeRepository employeeRepository, JobDetailRepository jobDetailRepository) {
        this.employeeRepository = employeeRepository;
        this.jobDetailRepository = jobDetailRepository;
    }

    @PostMapping("/{employeeCode}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
    public ResponseEntity<JobDetails> registerJobDetails(@PathVariable String employeeCode, @RequestBody JobDetailRequest jobDetailRequest){
        return (ResponseEntity<JobDetails>) employeeRepository.findByEmployeeCode(employeeCode)
                .map(employee -> {
                    // Prevent creating duplicate job details for the same employee
                    if (jobDetailRepository.findByEmployeeId(employee.getId()).isPresent()) {
                        // Return 409 Conflict if job details already exist
                        return ResponseEntity.status(409).build();
                    }

                    JobDetails jobDetails = new JobDetails();
                    jobDetails.setEmployee(employee);
                    jobDetails.setDepartmentTitle(jobDetailRequest.getDepartmentTitle());
                    jobDetails.setDesignationTitle(jobDetailRequest.getDesignationTitle());
                    jobDetails.setEmploymentType(jobDetailRequest.getEmploymentType());
                    jobDetails.setWorkMode(jobDetailRequest.getWorkMode());
                    jobDetails.setDoj(jobDetailRequest.getDoj());
                    jobDetails.setEndDate(jobDetailRequest.getEndDate());
                    jobDetails.setProbationEndDate(jobDetailRequest.getProbationEndDate());
                    jobDetails.setNoticePeriodDay(jobDetailRequest.getNoticePeriodDay());
                    jobDetails.setShift(jobDetailRequest.getShift());

                    JobDetails savedJobDetails = jobDetailRepository.save(jobDetails);

                    URI location = ServletUriComponentsBuilder
                            .fromCurrentContextPath().path("/api/jobdetails/{employeeCode}")
                            .buildAndExpand(employeeCode).toUri();

                    return ResponseEntity.created(location).body(savedJobDetails);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{employeeCode}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
    public ResponseEntity<JobDetails> updateJobdetails(@PathVariable String employeeCode, @RequestBody JobDetailRequest jobDetailRequest){
        return employeeRepository.findByEmployeeCode(employeeCode)
                .flatMap(employee -> jobDetailRepository.findByEmployeeId(employee.getId()))
                .map(jobDetails -> {
                    jobDetails.setDepartmentTitle(jobDetailRequest.getDepartmentTitle());
                    jobDetails.setDesignationTitle(jobDetailRequest.getDesignationTitle());
                    jobDetails.setEmploymentType(jobDetailRequest.getEmploymentType());
                    jobDetails.setWorkMode(jobDetailRequest.getWorkMode());
                    jobDetails.setDoj(jobDetailRequest.getDoj());
                    jobDetails.setEndDate(jobDetailRequest.getEndDate());
                    jobDetails.setProbationEndDate(jobDetailRequest.getProbationEndDate());
                    jobDetails.setNoticePeriodDay(jobDetailRequest.getNoticePeriodDay());
                    jobDetails.setShift(jobDetailRequest.getShift());
                    JobDetails updatedJobDetails = jobDetailRepository.save(jobDetails);
                    return ResponseEntity.ok(updatedJobDetails);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{employeeCode}")
    public ResponseEntity<JobDetails> getJobDetail(@PathVariable String employeeCode){
        return employeeRepository.findByEmployeeCode(employeeCode)
                .flatMap(employee -> jobDetailRepository.findByEmployeeId(employee.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
