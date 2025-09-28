package com.example.multi_tanent.tenant.attendance.controller;

import com.example.multi_tanent.tenant.attendance.dto.AttendanceRecordRequest;
import com.example.multi_tanent.tenant.attendance.entity.AttendanceRecord;
import com.example.multi_tanent.tenant.attendance.service.AttendanceRecordService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance-records")
@CrossOrigin(origins = "*")
public class AttendanceRecordController {

    private final AttendanceRecordService attendanceService;

    public AttendanceRecordController(AttendanceRecordService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
    public ResponseEntity<AttendanceRecord> markAttendance(@RequestBody AttendanceRecordRequest request) {
        AttendanceRecord newRecord = attendanceService.markAttendance(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newRecord.getId()).toUri();
        return ResponseEntity.created(location).body(newRecord);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceRecord> getAttendanceById(@PathVariable Long id) {
        return attendanceService.getAttendanceRecordById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeCode}")
    public ResponseEntity<List<AttendanceRecord>> getEmployeeAttendance(
            @PathVariable String employeeCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AttendanceRecord> records = attendanceService.getAttendanceForEmployee(employeeCode, startDate, endDate);
        return ResponseEntity.ok(records);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
    public ResponseEntity<AttendanceRecord> updateAttendance(@PathVariable Long id, @RequestBody AttendanceRecordRequest request) {
        AttendanceRecord updatedRecord = attendanceService.updateAttendance(id, request);
        return ResponseEntity.ok(updatedRecord);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
    public ResponseEntity<Void> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendanceRecord(id);
        return ResponseEntity.noContent().build();
    }
}
