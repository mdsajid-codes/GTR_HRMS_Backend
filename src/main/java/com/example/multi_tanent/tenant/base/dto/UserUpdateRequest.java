package com.example.multi_tanent.tenant.base.dto;

import com.example.multi_tanent.master.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    private String name;
    private Set<Role> roles;
    private Boolean isActive;
    private Long storeId; // Optional: to change the user's associated store
}