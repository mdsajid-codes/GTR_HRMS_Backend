package com.example.multi_tanent.tenant.tenantDto;

import java.time.LocalDate;

import com.example.multi_tanent.tenant.entity.enums.EmployeeStatus;
import com.example.multi_tanent.tenant.entity.enums.Gender;
import com.example.multi_tanent.tenant.entity.enums.MartialStatus;

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
    private String phoneSecondary;
    private LocalDate dob;
    private Gender gender;
    private MartialStatus martialStatus;
    private String currentAddress;
    private String permanentAddress;
    private String nationalIdType;
    private String nationalIdNumber;
    private EmployeeStatus status;
}
