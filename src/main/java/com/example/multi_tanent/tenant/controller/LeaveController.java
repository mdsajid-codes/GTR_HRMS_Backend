package com.example.multi_tanent.tenant.controller;

import java.time.LocalDateTime;

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

import com.example.multi_tanent.tenant.entity.Leave;
import com.example.multi_tanent.tenant.entity.enums.LeaveStatus;
import com.example.multi_tanent.tenant.repository.EmployeeRepository;
import com.example.multi_tanent.tenant.repository.LeaveRepository;
import com.example.multi_tanent.tenant.tenantDto.LeaveRequest;
import com.example.multi_tanent.tenant.tenantDto.LeaveUpdate;

@RestController
@RequestMapping("/api/leaves")
@CrossOrigin(origins = "*")
public class LeaveController {
    private EmployeeRepository employeeRepository;
    private LeaveRepository leaveRepository;

    public LeaveController(EmployeeRepository employeeRepository, LeaveRepository leaveRepository){
        this.employeeRepository = employeeRepository;
        this.leaveRepository = leaveRepository;
    }

    @PostMapping("/{employeeCode}")
    public ResponseEntity<String> registerLeave(@PathVariable String employeeCode, @RequestBody LeaveRequest leaveRequest){
        return employeeRepository.findByEmployeeCode(employeeCode)
                .map(employee -> {
                    Leave leave = new Leave();
                    leave.setEmployee(employee);
                    leave.setLeaveType(leaveRequest.getLeaveType());
                    leave.setStartDate(leaveRequest.getStartDate());
                    leave.setEndDate(leaveRequest.getEndDate());
                    leave.setReason(leaveRequest.getReason());
                    leave.setStatus(LeaveStatus.PENDING);
                    leave.setApprovedBy(null);
                    leave.setApprovedDate(null);
                    leave.setCreatedAt(LocalDateTime.now());
                    leave.setUpdatedAt(LocalDateTime.now());
                    leaveRepository.save(leave);
                    return ResponseEntity.ok("Leave registered successfully!");
                })
                .orElse(ResponseEntity.notFound().build());
                
    }

    @PutMapping("/{employeeCode}/{leaveId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR')")
    public ResponseEntity<String> updateLeave(@PathVariable String employeeCode, @PathVariable Long leaveId, @RequestBody LeaveUpdate leaveUpdate){
        return employeeRepository.findByEmployeeCode(employeeCode)
                // Find the specific leave for the given employee to ensure data integrity
                .flatMap(employee -> employee.getLeaves().stream().filter(l -> l.getId().equals(leaveId)).findFirst())
                .map(leave -> {
                    leave.setStatus(leaveUpdate.getStatus());
                    leave.setApprovedBy(leaveUpdate.getApprovedBy());
                    leave.setApprovedDate(LocalDateTime.now());
                    leave.setUpdatedAt(LocalDateTime.now());
                    leaveRepository.save(leave);
                    return ResponseEntity.ok("Leave updated successfully!");
                })
                .orElse(ResponseEntity.notFound().build());
                
    }

    @GetMapping("/{employeeCode}")
    public ResponseEntity<java.util.List<Leave>> getLeavesByEmployeeCode(@PathVariable String employeeCode){
        return employeeRepository.findByEmployeeCode(employeeCode)
                .map(employee -> ResponseEntity.ok(employee.getLeaves().stream().toList()))
                .orElse(ResponseEntity.notFound().build());
    }
    
}
