package com.example.multi_tanent.crm.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CrmRoleRequest {
    @NotBlank
    private String name;

    private String description;

    // optional: clone from an existing role (same tenant)
    private Long cloneRoleId;

    // optional: if you want to explicitly set permissions during create/update
    private List<Long> permissionIds;
}

