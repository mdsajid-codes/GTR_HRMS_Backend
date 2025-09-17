package com.example.multi_tanent.tenant.base.dto;

import java.util.Set;

import com.example.multi_tanent.master.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequest {
    private String name;
    private String email;
    private String passwordHash;
    private Set<Role> roles;
    private Boolean isActive;
    private Boolean isLocked;
    private Integer loginAttempts;
    private String lastLoginIp;
}
