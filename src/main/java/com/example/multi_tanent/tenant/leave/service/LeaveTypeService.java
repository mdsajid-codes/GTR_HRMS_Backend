package com.example.multi_tanent.tenant.leave.service;

import com.example.multi_tanent.tenant.leave.dto.LeaveTypeRequest;
import com.example.multi_tanent.tenant.leave.entity.LeaveType;
import com.example.multi_tanent.tenant.leave.repository.LeaveTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(transactionManager = "tenantTx")
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    public LeaveTypeService(LeaveTypeRepository leaveTypeRepository) {
        this.leaveTypeRepository = leaveTypeRepository;
    }

    public LeaveType createLeaveType(LeaveTypeRequest request) {
        leaveTypeRepository.findByLeaveType(request.getLeaveType()).ifPresent(lt -> {
            throw new RuntimeException("Leave type with name '" + request.getLeaveType() + "' already exists.");
        });

        LeaveType leaveType = new LeaveType();
        mapRequestToEntity(request, leaveType);
        return leaveTypeRepository.save(leaveType);
    }

    @Transactional(readOnly = true)
    public List<LeaveType> getAllLeaveTypes() {
        return leaveTypeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<LeaveType> getLeaveTypeById(Long id) {
        return leaveTypeRepository.findById(id);
    }

    public LeaveType updateLeaveType(Long id, LeaveTypeRequest request) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave type not found with id: " + id));

        mapRequestToEntity(request, leaveType);
        return leaveTypeRepository.save(leaveType);
    }

    public void deleteLeaveType(Long id) {
        if (!leaveTypeRepository.existsById(id)) {
            throw new RuntimeException("Leave type not found with id: " + id);
        }
        leaveTypeRepository.deleteById(id);
    }

    private void mapRequestToEntity(LeaveTypeRequest request, LeaveType leaveType) {
        leaveType.setLeaveType(request.getLeaveType());
        leaveType.setDescription(request.getDescription());
        leaveType.setIsPaid(request.getIsPaid());
        leaveType.setMaxDaysPerYear(request.getMaxDaysPerYear());
    }
}