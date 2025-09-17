package com.example.multi_tanent.tenant.employee.dto;

import lombok.Data;

@Data
public class EmployeeDocumentRequest {
    private String docType;
    private String remarks;
    private Boolean verified;
}