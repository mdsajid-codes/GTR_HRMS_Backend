package com.example.multi_tanent.tenant.attendance.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "biometric_devices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiometricDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String deviceIdentifier; // e.g., serial number

    private String deviceType; // Fingerprint, Face

    private String ipAddress;
    private Integer port;

    private String siteLattitude;
    private String siteLongitude;

    private String location; // office or gate location

    private Boolean active;
}
