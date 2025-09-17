package com.example.multi_tanent.tenant.employee.dto;

import lombok.Data;

@Data
public class TimeAttendenceRequest {
    private String timeType;
    private String workType;
    private String shiftType;
    private String weeklyOffPolicy;
    private String leaveGroup;
    private String attendenceCaptureScheme;
    private String holidayList;
    private String expensePolicy;
    private String attendenceTrackingPolicy;
    private String recruitmentPolicy;
    private Boolean isRosterBasedEmployee;
}