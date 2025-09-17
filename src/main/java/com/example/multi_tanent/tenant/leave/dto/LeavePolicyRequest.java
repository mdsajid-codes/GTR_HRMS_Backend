package com.example.multi_tanent.tenant.leave.dto;

import lombok.Data;

@Data
public class LeavePolicyRequest {
    private String policyName;
    private String leaveType;
    private Long jobBandId;
    private Integer allocatedDays;
    private Boolean active;
}