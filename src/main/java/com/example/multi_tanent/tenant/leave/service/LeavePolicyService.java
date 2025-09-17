package com.example.multi_tanent.tenant.leave.service;

import com.example.multi_tanent.tenant.base.entity.JobBand;
import com.example.multi_tanent.tenant.base.repository.JobBandRepository;
import com.example.multi_tanent.tenant.leave.dto.LeavePolicyRequest;
import com.example.multi_tanent.tenant.leave.entity.LeavePolicy;
import com.example.multi_tanent.tenant.leave.entity.LeaveType;
import com.example.multi_tanent.tenant.leave.repository.LeavePolicyRepository;
import com.example.multi_tanent.tenant.leave.repository.LeaveTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(transactionManager = "tenantTx")
public class LeavePolicyService {

    private final LeavePolicyRepository policyRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final JobBandRepository jobBandRepository;

    public LeavePolicyService(LeavePolicyRepository policyRepository,
                              LeaveTypeRepository leaveTypeRepository,
                              JobBandRepository jobBandRepository) {
        this.policyRepository = policyRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.jobBandRepository = jobBandRepository;
    }

    public LeavePolicy createPolicy(LeavePolicyRequest request) {
        LeaveType leaveType = leaveTypeRepository.findByLeaveType(request.getLeaveType())
                .orElseThrow(() -> new RuntimeException("Leave type '" + request.getLeaveType() + "' not found in database. Please create it first."));

        policyRepository.findByLeaveTypeIdAndJobBandId(leaveType.getId(), request.getJobBandId())
                .ifPresent(p -> {
                    throw new RuntimeException("A leave policy for this leave type and job band already exists.");
                });

        LeavePolicy policy = new LeavePolicy();
        mapRequestToEntity(request, policy);
        return policyRepository.save(policy);
    }

    @Transactional(readOnly = true)
    public List<LeavePolicy> getAllPolicies() {
        return policyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<LeavePolicy> getPolicyById(Long id) {
        return policyRepository.findById(id);
    }

    public LeavePolicy updatePolicy(Long id, LeavePolicyRequest request) {
        LeavePolicy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave policy not found with id: " + id));

        // Check for uniqueness if the combination of leave type and job band is being changed
        LeaveType newLeaveType = leaveTypeRepository.findByLeaveType(request.getLeaveType())
                .orElseThrow(() -> new RuntimeException("Leave type '" + request.getLeaveType() + "' not found in database. Please create it first."));

        policyRepository.findByLeaveTypeIdAndJobBandId(newLeaveType.getId(), request.getJobBandId()).ifPresent(existingPolicy -> {
            if (!existingPolicy.getId().equals(id)) {
                throw new RuntimeException("A leave policy for this leave type and job band already exists.");
            }
        });

        mapRequestToEntity(request, policy);
        return policyRepository.save(policy);
    }

    public void deletePolicy(Long id) {
        if (!policyRepository.existsById(id)) {
            throw new RuntimeException("Leave policy not found with id: " + id);
        }
        policyRepository.deleteById(id);
    }

    private void mapRequestToEntity(LeavePolicyRequest request, LeavePolicy policy) {
        LeaveType leaveType = leaveTypeRepository.findByLeaveType(request.getLeaveType())
                .orElseThrow(() -> new RuntimeException("Leave type '" + request.getLeaveType() + "' not found in database. Please create it first."));

        JobBand jobBand = jobBandRepository.findById(request.getJobBandId())
                .orElseThrow(() -> new RuntimeException("Job band not found with id: " + request.getJobBandId()));

        policy.setPolicyName(request.getPolicyName()); // This line was already present, no change needed here.
        policy.setLeaveType(leaveType);
        policy.setJobBand(jobBand);
        policy.setAllocatedDays(request.getAllocatedDays());
        if (request.getActive() != null) {
            policy.setActive(request.getActive());
        }
    }
}