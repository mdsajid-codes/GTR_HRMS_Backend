package com.example.multi_tanent.crm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CompanyTypeRequest {
    @NotBlank(message = "Type name is required")
    private String name;
}