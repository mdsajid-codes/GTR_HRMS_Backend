package com.example.multi_tanent.tenant.employee.controller;

import com.example.multi_tanent.tenant.employee.dto.TimeAttendenceRequest;
import com.example.multi_tanent.tenant.employee.entity.TimeAttendence;
import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository;
import com.example.multi_tanent.tenant.employee.repository.TimeAttendenceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/time-attendence")
@CrossOrigin(origins = "*")
@Transactional(transactionManager = "tenantTx")
public class TimeAttendenceController {
    private final EmployeeRepository employeeRepository;
    private final TimeAttendenceRepository timeAttendenceRepository;

    public TimeAttendenceController(EmployeeRepository employeeRepository, TimeAttendenceRepository timeAttendenceRepository) {
        this.employeeRepository = employeeRepository;
        this.timeAttendenceRepository = timeAttendenceRepository;
    }

    @PutMapping("/{employeeCode}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR')")
    public ResponseEntity<TimeAttendence> createOrUpdateTimeAttendence(@PathVariable String employeeCode, @RequestBody TimeAttendenceRequest request) {
        return employeeRepository.findByEmployeeCode(employeeCode)
                .map(employee -> {
                    TimeAttendence timeAttendence = timeAttendenceRepository.findByEmployeeId(employee.getId())
                            .orElse(new TimeAttendence());

                    boolean isNew = timeAttendence.getId() == null;
                    if (isNew) {
                        timeAttendence.setEmployee(employee);
                    }

                    updateTimeAttendenceFromRequest(timeAttendence, request);
                    TimeAttendence savedTimeAttendence = timeAttendenceRepository.save(timeAttendence);

                    if (isNew) {
                        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/api/time-attendence/{employeeCode}")
                                .buildAndExpand(employeeCode).toUri();
                        return ResponseEntity.created(location).body(savedTimeAttendence);
                    } else {
                        return ResponseEntity.ok(savedTimeAttendence);
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{employeeCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TimeAttendence> getTimeAttendence(@PathVariable String employeeCode) {
        return timeAttendenceRepository.findByEmployeeEmployeeCode(employeeCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{employeeCode}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR')")
    public ResponseEntity<Void> deleteTimeAttendence(@PathVariable String employeeCode) {
        return timeAttendenceRepository.findByEmployeeEmployeeCode(employeeCode)
                .map(timeAttendence -> {
                    timeAttendenceRepository.delete(timeAttendence);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private void updateTimeAttendenceFromRequest(TimeAttendence timeAttendence, TimeAttendenceRequest request) {
        timeAttendence.setTimeType(request.getTimeType());
        timeAttendence.setWorkType(request.getWorkType());
        timeAttendence.setShiftType(request.getShiftType());
        timeAttendence.setWeeklyOffPolicy(request.getWeeklyOffPolicy());
        timeAttendence.setLeaveGroup(request.getLeaveGroup());
        timeAttendence.setAttendenceCaptureScheme(request.getAttendenceCaptureScheme());
        timeAttendence.setHolidayList(request.getHolidayList());
        timeAttendence.setExpensePolicy(request.getExpensePolicy());
        timeAttendence.setAttendenceTrackingPolicy(request.getAttendenceTrackingPolicy());
        timeAttendence.setRecruitmentPolicy(request.getRecruitmentPolicy());
        timeAttendence.setIsRosterBasedEmployee(request.getIsRosterBasedEmployee());
    }
}
