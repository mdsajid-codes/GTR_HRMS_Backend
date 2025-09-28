package com.example.multi_tanent.master.dto;

import com.example.multi_tanent.master.entity.ServiceModule;
import com.example.multi_tanent.master.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

public record ProvisionTenantRequest(
        @NotBlank(message = "Tenant ID is required")
        @Size(min = 3, max = 64, message = "Tenant ID must be between 3 and 64 characters")
        String tenantId,

        @NotBlank(message = "Company name is required")
        String companyName,

        @NotBlank(message = "Admin email is required")
        @Email(message = "Invalid email format")
        String adminEmail,

        @NotBlank(message = "Admin password is required")
        String adminPassword,

        @NotEmpty(message = "At least one service module is required")
        List<ServiceModule> serviceModules,

        @NotEmpty(message = "At least one admin role is required")
        Set<Role> adminRoles
) {}