package com.example.multi_tanent.tenant.leave.dto;

import com.example.multi_tanent.tenant.leave.entity.LeaveRequest;
import com.example.multi_tanent.tenant.leave.enums.LeaveStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestResponseDto {
    private Long id;
    private String employeeCode;
    private String employeeName;
    private String leaveType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal daysRequested;
    private String partialDayInfo;
    private String reason;
    private LeaveStatus status;
    private Long currentApproverUserId;
    private BigDecimal daysApproved;
    private Long createdByUserId;
    private LocalDateTime createdAt;
    private Long updatedByUserId;
    private LocalDateTime updatedAt;
    private String adminNotes;

    public static LeaveRequestResponseDto fromEntity(LeaveRequest entity) {
        if (entity == null) {
            return null;
        }

        // Accessing lazy-loaded properties while the session is still open
        String employeeName = entity.getEmployee() != null ? (entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName()).trim() : null;

        return new LeaveRequestResponseDto(
                entity.getId(),
                entity.getEmployee() != null ? entity.getEmployee().getEmployeeCode() : null,
                employeeName,
                entity.getLeaveType() != null ? entity.getLeaveType().getLeaveType() : null,
                entity.getFromDate(),
                entity.getToDate(),
                entity.getDaysRequested(),
                entity.getPartialDayInfo(),
                entity.getReason(),
                entity.getStatus(),
                entity.getCurrentApproverUserId(),
                entity.getDaysApproved(),
                entity.getCreatedByUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedByUserId(),
                entity.getUpdatedAt(),
                entity.getAdminNotes()
        );
    }
}