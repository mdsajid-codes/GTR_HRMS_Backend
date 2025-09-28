package com.example.multi_tanent.tenant.employee.controller;

import com.example.multi_tanent.tenant.employee.dto.JobDetailsRequest;
import com.example.multi_tanent.tenant.employee.entity.JobDetails;
import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository;
import com.example.multi_tanent.tenant.employee.repository.JobDetailsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/job-details")
@CrossOrigin(origins = "*")
@Transactional(transactionManager = "tenantTx")
public class JobDetailsController {

    private final EmployeeRepository employeeRepository;
    private final JobDetailsRepository jobDetailsRepository;

    public JobDetailsController(EmployeeRepository employeeRepository, JobDetailsRepository jobDetailsRepository) {
        this.employeeRepository = employeeRepository;
        this.jobDetailsRepository = jobDetailsRepository;
    }

    @PutMapping("/{employeeCode}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
    public ResponseEntity<JobDetails> createOrUpdateJobDetails(@PathVariable String employeeCode, @RequestBody JobDetailsRequest request) {
        return employeeRepository.findByEmployeeCode(employeeCode)
                .map(employee -> {
                    JobDetails jobDetails = jobDetailsRepository.findByEmployeeId(employee.getId())
                            .orElse(new JobDetails());

                    boolean isNew = jobDetails.getId() == null;
                    if (isNew) {
                        jobDetails.setEmployee(employee);
                    }

                    updateJobDetailsFromRequest(jobDetails, request);
                    JobDetails savedJobDetails = jobDetailsRepository.save(jobDetails);

                    if (isNew) {
                        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/api/job-details/{employeeCode}")
                                .buildAndExpand(employeeCode).toUri();
                        return ResponseEntity.created(location).body(savedJobDetails);
                    } else {
                        return ResponseEntity.ok(savedJobDetails);
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{employeeCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JobDetails> getJobDetails(@PathVariable String employeeCode) {
        return employeeRepository.findByEmployeeCode(employeeCode)
                .flatMap(employee -> jobDetailsRepository.findByEmployeeId(employee.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private void updateJobDetailsFromRequest(JobDetails jobDetails, JobDetailsRequest request) {
        jobDetails.setLocation(request.getLocation());
        jobDetails.setActualLocation(request.getActualLocation());
        jobDetails.setDepartment(request.getDepartment());
        jobDetails.setDesignation(request.getDesignation());
        jobDetails.setJobBand(request.getJobBand());
        jobDetails.setReportsTo(request.getReportsTo());
        jobDetails.setDateOfJoining(request.getDateOfJoining());
        jobDetails.setProbationEndDate(request.getProbationEndDate());
        jobDetails.setLoginId(request.getLoginId());
        jobDetails.setProfileName(request.getProfileName());
        jobDetails.setEmployeeNumber(request.getEmployeeNumber());
        jobDetails.setLegalEntity(request.getLegalEntity());
    }
}
