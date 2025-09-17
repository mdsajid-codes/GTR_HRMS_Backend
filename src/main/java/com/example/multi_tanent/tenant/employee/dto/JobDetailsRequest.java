package com.example.multi_tanent.tenant.employee.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class JobDetailsRequest {
    private String location;
    private String actualLocation;
    private String department;
    private String designation;
    private String jobBand;
    private String reportsTo;
    private LocalDate dateOfJoining;
    private LocalDate probationEndDate;
    private String loginId;
    private String profileName;
    private String employeeNumber;
    private String legalEntity;
}