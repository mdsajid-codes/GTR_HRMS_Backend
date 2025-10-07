package com.example.multi_tanent.tenant.employee.dto;

import java.time.LocalDate;

import com.example.multi_tanent.tenant.employee.enums.EmployeeStatus;
import com.example.multi_tanent.tenant.employee.enums.Gender;
import com.example.multi_tanent.tenant.employee.enums.MartialStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {
    private String email;
    private String employeeCode;
    private String firstName;
    private String middleName;
    private String lastName;
    private String emailWork;
    private String emailPersonal;
    private String phonePrimary;
    private LocalDate dob;
    private Gender gender;
    private MartialStatus martialStatus;
    private EmployeeStatus status;
    private String photoPath;
    private Long locationId;
}
