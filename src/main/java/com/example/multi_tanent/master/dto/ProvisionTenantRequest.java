package com.example.multi_tanent.master.dto;

public record ProvisionTenantRequest(
    String tenantId,
    String companyName,
    String adminEmail,
    String adminPassword
) {}
