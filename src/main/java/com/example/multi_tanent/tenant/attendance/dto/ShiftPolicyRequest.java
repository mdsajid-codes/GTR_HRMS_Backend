package com.example.multi_tanent.tenant.attendance.dto;

import lombok.Data;
import java.time.LocalTime;

@Data
public class ShiftPolicyRequest {
    private String policyName;
    private LocalTime shiftStartTime;
    private LocalTime shiftEndTime;
    private Integer gracePeriodMinutes;
    private Integer graceHalfDayMinutes;
    private Boolean isDefault;
    private String description;
}

