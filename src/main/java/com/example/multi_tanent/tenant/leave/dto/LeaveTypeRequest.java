package com.example.multi_tanent.tenant.leave.dto;


import lombok.Data;

@Data
public class LeaveTypeRequest {
    private String leaveType;
    private String description;
    private Boolean isPaid;
    private Integer maxDaysPerYear;
}