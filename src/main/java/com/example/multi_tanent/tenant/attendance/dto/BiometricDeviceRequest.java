package com.example.multi_tanent.tenant.attendance.dto;

import lombok.Data;

@Data
public class BiometricDeviceRequest {
    private String deviceIdentifier;
    private String deviceType;
    private String ipAddress;
    private Integer port;
    private String siteLattitude;
    private String siteLongitude;
    private String location; // Can be provided manually if lat/long are not available
    private Boolean active;
}