package com.example.multi_tanent.tenant.tenantDto;

import java.time.LocalDate;

import com.example.multi_tanent.tenant.entity.enums.LeaveType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest {
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
}
