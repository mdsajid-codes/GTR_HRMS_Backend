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

    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (tenantId == null) {
            tenantId = TenantContext.getTenantId();
        }
    }
}