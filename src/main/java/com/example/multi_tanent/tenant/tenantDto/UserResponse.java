package com.example.multi_tanent.tenant.tenantDto;

import com.example.multi_tanent.tenant.entity.User;
import com.example.multi_tanent.tenant.entity.enums.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Set<Role> roles;
    private Boolean isActive;
    private Boolean isLocked;
    private Integer loginAttempts;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse fromEntity(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRoles(user.getRoles());
        dto.setIsActive(user.getIsActive());
        dto.setIsLocked(user.getIsLocked());
        dto.setLoginAttempts(user.getLoginAttempts());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setLastLoginIp(user.getLastLoginIp());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}