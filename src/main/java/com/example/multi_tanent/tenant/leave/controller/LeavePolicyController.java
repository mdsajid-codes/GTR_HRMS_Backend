package com.example.multi_tanent.tenant.leave.controller;

import com.example.multi_tanent.tenant.leave.dto.LeavePolicyRequest;
import com.example.multi_tanent.tenant.leave.entity.LeavePolicy;
import com.example.multi_tanent.tenant.leave.service.LeavePolicyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/leave-policies")
@CrossOrigin(origins = "*")
public class LeavePolicyController {
    private final LeavePolicyService policyService;

    public LeavePolicyController(LeavePolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
    public ResponseEntity<LeavePolicy> createPolicy(@RequestBody LeavePolicyRequest request) {
        LeavePolicy createdPolicy = policyService.createPolicy(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdPolicy.getId()).toUri();
        return ResponseEntity.created(location).body(createdPolicy);
    }

    @GetMapping
    public ResponseEntity<List<LeavePolicy>> getAllPolicies() {
        return ResponseEntity.ok(policyService.getAllPolicies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeavePolicy> getPolicyById(@PathVariable Long id) {
        return policyService.getPolicyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
    public ResponseEntity<LeavePolicy> updatePolicy(@PathVariable Long id, @RequestBody LeavePolicyRequest request) {
        LeavePolicy updatedPolicy = policyService.updatePolicy(id, request);
        return ResponseEntity.ok(updatedPolicy);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
    public ResponseEntity<Void> deletePolicy(@PathVariable Long id) {
        policyService.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }
}
