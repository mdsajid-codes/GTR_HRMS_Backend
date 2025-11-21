package com.example.multi_tanent.spersusers.enitity;

import com.example.multi_tanent.config.TenantContext;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tenants")
public class Tenant {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(nullable = false, unique = true)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    private String logoImgUrl;

    private String contactEmail;

    private String contactPhone;

    private String address;

    // SMTP Configuration
    @Column(name = "smtp_host")
    private String smtpHost;

    @Column(name = "smtp_port")
    private Integer smtpPort;

    @Column(name = "smtp_username")
    private String smtpUsername;

    @Column(name = "smtp_password")
    private String smtpPassword; // In a real app, this should be encrypted

    @Column(name = "company_email")
    private String companyEmail; // Default 'from' address for this tenant

    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (tenantId == null) {
            tenantId = TenantContext.getTenantId();
        }
    }
}