package com.example.multi_tanent.tenant.attendance.controller;

import com.example.multi_tanent.tenant.attendance.dto.BiometricPunchRequest;
import com.example.multi_tanent.tenant.attendance.entity.AttendanceRecord;
import com.example.multi_tanent.tenant.attendance.service.AttendanceRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/biometric-punch")
@CrossOrigin(origins = "*")
public class BiometricPunchController {

    private final AttendanceRecordService attendanceService;

    public BiometricPunchController(AttendanceRecordService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * Endpoint for biometric devices to post attendance punches.
     * The {tenantId} is used by a filter to set the correct database context.
     * This endpoint should be secured by IP whitelisting or an API key in a real-world scenario.
     * @param tenantId The ID of the tenant who owns the device.
     * @param request The punch data from the device.
     * @return The created or updated AttendanceRecord.
     */
    @PostMapping("/{tenantId}")
    public ResponseEntity<AttendanceRecord> recordPunch(@PathVariable String tenantId, @RequestBody BiometricPunchRequest request) {
        // The tenantId in the path is used by BiometricTenantFilter to set the database context.
        AttendanceRecord record = attendanceService.processBiometricPunch(request);
        return ResponseEntity.ok(record);
    }
}