package com.example.multi_tanent.master.dto;

import com.example.multi_tanent.master.entity.TenantPlan;

public record ProvisionTenantRequest(
    String tenantId,
    String companyName,
    String adminEmail,
    String adminPassword,
    TenantPlan plan
) {}