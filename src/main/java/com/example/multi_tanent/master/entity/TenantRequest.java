package com.example.multi_tanent.master.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tenantId;
    private String companyName;
    String adminEmail;
    String adminPassword; // Note: Storing plain-text password is a security risk.

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private List<ServiceModule> serviceModules;
}
