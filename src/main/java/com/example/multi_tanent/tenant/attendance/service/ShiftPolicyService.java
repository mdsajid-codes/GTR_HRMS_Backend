package com.example.multi_tanent.tenant.attendance.service;

import com.example.multi_tanent.tenant.attendance.dto.ShiftPolicyRequest;
import com.example.multi_tanent.tenant.attendance.entity.ShiftPolicy;
import com.example.multi_tanent.tenant.attendance.repository.ShiftPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(transactionManager = "tenantTx")
public class ShiftPolicyService {

    private final ShiftPolicyRepository shiftPolicyRepository;

    public ShiftPolicyService(ShiftPolicyRepository shiftPolicyRepository) {
        this.shiftPolicyRepository = shiftPolicyRepository;
    }

    public ShiftPolicy createShiftPolicy(ShiftPolicyRequest request) {
        shiftPolicyRepository.findByPolicyName(request.getPolicyName()).ifPresent(p -> {
            throw new RuntimeException("Shift policy with name '" + request.getPolicyName() + "' already exists.");
        });

        ShiftPolicy policy = new ShiftPolicy();
        policy.setPolicyName(request.getPolicyName());
        policy.setShiftStartTime(request.getShiftStartTime());
        policy.setShiftEndTime(request.getShiftEndTime());
        policy.setGracePeriodMinutes(request.getGracePeriodMinutes());
        policy.setGraceHalfDayMinutes(request.getGraceHalfDayMinutes());
        policy.setIsDefault(request.getIsDefault());
        policy.setDescription(request.getDescription());

        return shiftPolicyRepository.save(policy);
    }

    @Transactional(readOnly = true)
    public List<ShiftPolicy> getAllShiftPolicies() {
        return shiftPolicyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ShiftPolicy> getShiftPolicyById(Long id) {
        return shiftPolicyRepository.findById(id);
    }

    public ShiftPolicy updateShiftPolicy(Long id, ShiftPolicyRequest request) {
        ShiftPolicy policy = shiftPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shift policy not found with id: " + id));

        // Check if name is being changed to one that already exists
        if (!policy.getPolicyName().equals(request.getPolicyName())) {
            shiftPolicyRepository.findByPolicyName(request.getPolicyName()).ifPresent(p -> {
                throw new RuntimeException("Shift policy with name '" + request.getPolicyName() + "' already exists.");
            });
        }

        policy.setPolicyName(request.getPolicyName());
        policy.setShiftStartTime(request.getShiftStartTime());
        policy.setShiftEndTime(request.getShiftEndTime());
        policy.setGracePeriodMinutes(request.getGracePeriodMinutes());
        policy.setGraceHalfDayMinutes(request.getGraceHalfDayMinutes());
        policy.setIsDefault(request.getIsDefault());
        policy.setDescription(request.getDescription());

        return shiftPolicyRepository.save(policy);
    }

    public void deleteShiftPolicy(Long id) {
        if (!shiftPolicyRepository.existsById(id)) {
            throw new RuntimeException("Shift policy not found with id: " + id);
        }
        shiftPolicyRepository.deleteById(id);
    }
}

