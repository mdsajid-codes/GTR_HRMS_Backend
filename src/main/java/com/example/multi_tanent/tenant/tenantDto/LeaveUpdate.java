package com.example.multi_tanent.tenant.tenantDto;


import com.example.multi_tanent.tenant.entity.enums.LeaveStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveUpdate {
    private LeaveStatus status;
    private String approvedBy;
}
