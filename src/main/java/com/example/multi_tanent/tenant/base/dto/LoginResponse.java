package com.example.multi_tanent.tenant.base.dto;

import java.util.List;

import com.example.multi_tanent.master.entity.TenantPlan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private List<String> roles;
    private TenantPlan plan;
}

