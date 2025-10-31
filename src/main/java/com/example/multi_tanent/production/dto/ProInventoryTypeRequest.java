package com.example.multi_tanent.production.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProInventoryTypeRequest {

    @NotBlank(message = "Inventory type name is required.")
    @Size(max = 255, message = "Name cannot exceed 255 characters.")
    private String name;

    private String description;

    private Long locationId; // Optional: ID of the location to associate with
}
