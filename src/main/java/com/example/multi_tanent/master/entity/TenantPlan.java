package com.example.multi_tanent.master.entity;

import java.util.List;

public enum TenantPlan {
    // Tier 1: Core HR - Employee Management
    STARTER(
            "com.example.multi_tanent.tenant.base.entity",
            "com.example.multi_tanent.tenant.employee.entity"
    ),

    // Tier 2: STARTER + Attendance & Leave Management
    STANDARD(
            "com.example.multi_tanent.tenant.base.entity",
            "com.example.multi_tanent.tenant.employee.entity",
            "com.example.multi_tanent.tenant.attendance.entity",
            "com.example.multi_tanent.tenant.leave.entity"
    ),

    // Tier 3: STANDARD + Payroll Management
    PREMIUM(
            "com.example.multi_tanent.tenant.base.entity",
            "com.example.multi_tanent.tenant.employee.entity",
            "com.example.multi_tanent.tenant.attendance.entity",
            "com.example.multi_tanent.tenant.leave.entity",
            "com.example.multi_tanent.tenant.payroll.entity"
    ),

    // Tier 4: PREMIUM + Recruitment Module
    ENTERPRISE(
            "com.example.multi_tanent.tenant.base.entity",
            "com.example.multi_tanent.tenant.employee.entity",
            "com.example.multi_tanent.tenant.attendance.entity",
            "com.example.multi_tanent.tenant.leave.entity",
            "com.example.multi_tanent.tenant.payroll.entity",
            "com.example.multi_tanent.tenant.recruitment.entity"
    );

    private final List<String> entityPackages;

    TenantPlan(String... packages) { this.entityPackages = List.of(packages); }

    public String[] getEntityPackages() { return entityPackages.toArray(new String[0]); }
}
