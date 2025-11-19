package com.example.multi_tanent.sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SalesTermAndConditionRequest {
    @NotBlank(message = "Name is required.")
    @Size(max = 150, message = "Name cannot exceed 150 characters.")
    private String name;

    @NotBlank(message = "Content is required.")
    private String content;

    private boolean isDefault;
}