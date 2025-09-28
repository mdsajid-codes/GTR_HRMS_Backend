package com.example.multi_tanent.tenant.attendance.service;

import com.example.multi_tanent.spersusers.enitity.Employee;
import com.example.multi_tanent.tenant.attendance.dto.AttendanceRecordRequest;
import com.example.multi_tanent.tenant.attendance.dto.BiometricPunchRequest;
import com.example.multi_tanent.tenant.attendance.entity.AttendanceRecord;
import com.example.multi_tanent.tenant.attendance.entity.AttendanceSetting;
import com.example.multi_tanent.tenant.attendance.entity.BiometricDevice;
import com.example.multi_tanent.tenant.attendance.entity.EmployeeBiometricMapping;
import com.example.multi_tanent.tenant.attendance.entity.ShiftPolicy;
import com.example.multi_tanent.tenant.attendance.enums.AttendanceStatus;
import com.example.multi_tanent.tenant.attendance.repository.AttendanceRecordRepository;
import com.example.multi_tanent.tenant.attendance.repository.AttendanceSettingRepository;
import com.example.multi_tanent.tenant.attendance.repository.BiometricDeviceRepository;
import com.example.multi_tanent.tenant.attendance.repository.EmployeeBiometricMappingRepository;
import com.example.multi_tanent.tenant.attendance.repository.ShiftPolicyRepository;
import com.example.multi_tanent.tenant.employee.enums.EmployeeStatus;
import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository;
import com.example.multi_tanent.tenant.leave.repository.LeaveRequestRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(transactionManager = "tenantTx")
public class AttendanceRecordService {

    private final AttendanceRecordRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftPolicyRepository shiftPolicyRepository;
    private final AttendanceSettingRepository attendanceSettingRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeBiometricMappingRepository mappingRepository;
    private final BiometricDeviceRepository deviceRepository;

    public AttendanceRecordService(AttendanceRecordRepository attendanceRepository,
                                   EmployeeRepository employeeRepository,
                                   ShiftPolicyRepository shiftPolicyRepository,
                                   AttendanceSettingRepository attendanceSettingRepository,
                                   LeaveRequestRepository leaveRequestRepository,
                                   EmployeeBiometricMappingRepository mappingRepository,
                                   BiometricDeviceRepository deviceRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.shiftPolicyRepository = shiftPolicyRepository;
        this.attendanceSettingRepository = attendanceSettingRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.mappingRepository = mappingRepository;
        this.deviceRepository = deviceRepository;
    }

    public AttendanceRecord markAttendance(AttendanceRecordRequest request) {
        attendanceRepository.findByEmployeeEmployeeCodeAndAttendanceDate(request.getEmployeeCode(), request.getAttendanceDate())
                .ifPresent(rec -> {
                    throw new RuntimeException("Attendance record for employee " + request.getEmployeeCode() + " on " + request.getAttendanceDate() + " already exists.");
                });

        Employee employee = employeeRepository.findByEmployeeCode(request.getEmployeeCode())
                .orElseThrow(() -> new RuntimeException("Employee not found with code: " + request.getEmployeeCode()));

        AttendanceRecord record = new AttendanceRecord();
        record.setEmployee(employee);
        record.setAttendanceDate(request.getAttendanceDate());
        record.setCheckIn(request.getCheckIn());
        record.setCheckOut(request.getCheckOut());
        record.setRemarks(request.getRemarks());

        // If status is provided manually (e.g., ON_LEAVE, ABSENT), use it. Otherwise, determine from check-in.
        if (request.getStatus() != null) {
            record.setStatus(request.getStatus());
        } else {
            record.setStatus(request.getCheckIn() != null ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT);
        }

        // Apply shift logic only if the employee is present
        if (record.getStatus() == AttendanceStatus.PRESENT) {
            applyShiftPolicyLogic(record, request.getShiftPolicyId());
        }

        return attendanceRepository.save(record);
    }

    public AttendanceRecord updateAttendance(Long id, AttendanceRecordRequest request) {
        AttendanceRecord record = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance record not found with id: " + id));

        record.setCheckIn(request.getCheckIn());
        record.setCheckOut(request.getCheckOut());
        record.setRemarks(request.getRemarks());

        if (request.getStatus() != null) {
            record.setStatus(request.getStatus());
        } else {
            record.setStatus(request.getCheckIn() != null ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT);
        }

        // Reset calculated fields before reapplying logic
        record.setIsLate(false);
        record.setOvertimeMinutes(0);

        if (record.getStatus() == AttendanceStatus.PRESENT) {
            applyShiftPolicyLogic(record, request.getShiftPolicyId());
        } else {
            record.setShiftPolicy(null);
        }

        return attendanceRepository.save(record);
    }

    private void applyShiftPolicyLogic(AttendanceRecord record, Long overrideShiftPolicyId) {
        ShiftPolicy shiftPolicy = findShiftPolicy(overrideShiftPolicyId);

        if (shiftPolicy == null) {
            // No shift policy found, so we can't calculate late status or overtime.
            return;
        }

        record.setShiftPolicy(shiftPolicy);

        // Calculate Late Status
        if (record.getCheckIn() != null) {
            LocalTime lateThreshold = shiftPolicy.getShiftStartTime().plusMinutes(shiftPolicy.getGracePeriodMinutes());
            if (record.getCheckIn().isAfter(lateThreshold)) {
                record.setIsLate(true);
            }
        }

        // Calculate Half_day Status
        if (record.getCheckIn() != null){
            LocalTime halfDayThreshold = shiftPolicy.getShiftStartTime().plusMinutes(shiftPolicy.getGraceHalfDayMinutes());
            if (record.getCheckIn().isAfter(halfDayThreshold)) {
                record.setStatus(AttendanceStatus.HALF_DAY);
            }
        }

        // Calculate Overtime
        if (record.getCheckOut() != null && record.getCheckOut().isAfter(shiftPolicy.getShiftEndTime())) {
            long overtime = Duration.between(shiftPolicy.getShiftEndTime(), record.getCheckOut()).toMinutes();
            record.setOvertimeMinutes((int) overtime);
        }
    }

    private ShiftPolicy findShiftPolicy(Long overrideShiftPolicyId) {
        // Priority: Override ID > Default policy
        return Optional.ofNullable(overrideShiftPolicyId)
                .flatMap(shiftPolicyRepository::findById)
                .or(() -> shiftPolicyRepository.findByIsDefaultTrue())
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Optional<AttendanceRecord> getAttendanceRecordById(Long id) {
        return attendanceRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecord> getAttendanceForEmployee(String employeeCode, LocalDate startDate, LocalDate endDate) {
        employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new RuntimeException("Employee not found with code: " + employeeCode));
        return attendanceRepository.findByEmployeeEmployeeCodeAndAttendanceDateBetween(employeeCode, startDate, endDate);
    }

    public void deleteAttendanceRecord(Long id) {
        if (!attendanceRepository.existsById(id)) {
            throw new RuntimeException("Attendance record not found with id: " + id);
        }
        attendanceRepository.deleteById(id);
    }

    /**
     * Processes an attendance punch from a biometric device.
     * It finds the corresponding employee and creates or updates their attendance record for the day.
     *
     * @param punchRequest The data from the biometric device.
     * @return The saved AttendanceRecord.
     */
    public AttendanceRecord processBiometricPunch(BiometricPunchRequest punchRequest) {
        // 1. Find device
        BiometricDevice device = deviceRepository.findByDeviceIdentifier(punchRequest.getDeviceIdentifier())
                .orElseThrow(() -> new RuntimeException("Biometric device with identifier '" + punchRequest.getDeviceIdentifier() + "' not found."));

        // 2. Find employee mapping for the given device
        EmployeeBiometricMapping mapping = mappingRepository.findByBiometricIdentifierAndDeviceId(punchRequest.getBiometricIdentifier(), device.getId())
                .orElseThrow(() -> new RuntimeException("No employee mapping found for biometric ID '" + punchRequest.getBiometricIdentifier() + "' on device '" + device.getDeviceIdentifier() + "'."));

        if (!Boolean.TRUE.equals(mapping.getActive())) {
            throw new RuntimeException("Employee mapping is inactive for biometric ID: " + punchRequest.getBiometricIdentifier());
        }

        Employee employee = mapping.getEmployee();
        LocalDateTime punchTime = punchRequest.getPunchTime();
        LocalDate punchDate = punchTime.toLocalDate();
        LocalTime punchLocalTime = punchTime.toLocalTime();

        // 3. Find or create an attendance record for the day
        AttendanceRecord record = attendanceRepository.findByEmployeeEmployeeCodeAndAttendanceDate(employee.getEmployeeCode(), punchDate)
                .orElse(new AttendanceRecord());

        // 4. Logic for check-in vs check-out. First punch is check-in, subsequent punches update check-out.
        if (record.getId() == null) { // This is a new record for the day (first punch)
            record.setEmployee(employee);
            record.setAttendanceDate(punchDate);
            record.setCheckIn(punchLocalTime);
            record.setStatus(AttendanceStatus.PRESENT);
            applyShiftPolicyLogic(record, null); // Apply default shift policy
        } else { // This is a subsequent punch for the day
            record.setCheckOut(punchLocalTime);
            // Re-apply logic to calculate overtime if applicable
            applyShiftPolicyLogic(record, record.getShiftPolicy() != null ? record.getShiftPolicy().getId() : null);
        }

        return attendanceRepository.save(record);
    }

    /**
     * Scheduled task to automatically mark employees as absent if they haven't checked in.
     * This runs daily at a configured time (e.g., 6 PM).
     * Note: For this to work in a multi-tenant environment, the scheduler must be adapted to
     * iterate over all tenants and execute this logic within each tenant's context.
     */
    @Scheduled(cron = "0 0 18 * * ?") // Runs every day at 6 PM server time.
    public void autoMarkAbsentEmployees() {
        LocalDate today = LocalDate.now();

        Optional<AttendanceSetting> settingOpt = attendanceSettingRepository.findAll().stream().findFirst();
        if (settingOpt.isEmpty() || !Boolean.TRUE.equals(settingOpt.get().getAutoMarkAbsentAfter())) {
            return; // Feature is disabled for this tenant.
        }

        List<Employee> activeEmployees = employeeRepository.findByStatus(EmployeeStatus.ACTIVE);

        for (Employee employee : activeEmployees) {
            boolean recordExists = attendanceRepository.findByEmployeeEmployeeCodeAndAttendanceDate(employee.getEmployeeCode(), today).isPresent();

            if (!recordExists) {
                // Before marking absent, check if the employee is on an approved leave.
                boolean onLeave = leaveRequestRepository.findByEmployeeId(employee.getId()).stream()
                        .anyMatch(leave -> leave.getStatus() == com.example.multi_tanent.tenant.leave.enums.LeaveStatus.APPROVED &&
                                !today.isBefore(leave.getFromDate()) && !today.isAfter(leave.getToDate()));

                AttendanceRecord newRecord = new AttendanceRecord();
                newRecord.setEmployee(employee);
                newRecord.setAttendanceDate(today);

                if (onLeave) {
                    newRecord.setStatus(AttendanceStatus.ON_LEAVE);
                    newRecord.setRemarks("Auto-marked as ON LEAVE based on approved leave.");
                } else {
                    // Further checks for holidays or weekly offs could be added here.
                    newRecord.setStatus(AttendanceStatus.ABSENT);
                    newRecord.setRemarks("Auto-marked as ABSENT. No check-in recorded.");
                }
                attendanceRepository.save(newRecord);
            }
        }
    }
}