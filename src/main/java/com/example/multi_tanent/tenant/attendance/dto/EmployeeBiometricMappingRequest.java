package com.example.multi_tanent.tenant.attendance.dto;

import com.example.multi_tanent.tenant.attendance.enums.IdentifierType;
import lombok.Data;

@Data
public class EmployeeBiometricMappingRequest {
    private String employeeCode;
    private Long deviceId;
    private String biometricIdentifier;
    private IdentifierType identifierType;
    private Boolean active;
    private String externalReference;
}