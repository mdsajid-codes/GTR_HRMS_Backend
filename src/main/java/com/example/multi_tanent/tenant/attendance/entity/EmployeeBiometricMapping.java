package com.example.multi_tanent.tenant.attendance.entity;

import com.example.multi_tanent.tenant.attendance.enums.IdentifierType;
import com.example.multi_tanent.tenant.employee.entity.Employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee_biometric_mappings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeBiometricMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private BiometricDevice device;

    @Column(nullable = false)
    private String biometricIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdentifierType identifierType;

    private Boolean active;
    private String externalReference; // e.g., vendor-specific template ID

    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
