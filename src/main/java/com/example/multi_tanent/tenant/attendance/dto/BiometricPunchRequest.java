package com.example.multi_tanent.tenant.attendance.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BiometricPunchRequest {
    private String deviceIdentifier; // Serial number of the device
    private String biometricIdentifier; // The ID from the device (e.g., fingerprint ID, card ID)
    private LocalDateTime punchTime; // The timestamp of the punch from the device
}