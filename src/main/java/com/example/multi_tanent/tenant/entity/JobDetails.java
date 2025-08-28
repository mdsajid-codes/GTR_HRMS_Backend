package com.example.multi_tanent.tenant.entity;

import java.time.LocalDate;

import com.example.multi_tanent.tenant.entity.enums.EmployeeShift;
import com.example.multi_tanent.tenant.entity.enums.EmploymentType;
import com.example.multi_tanent.tenant.entity.enums.WorkMode;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private String departmentTitle;
    private String designationTitle;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    @Enumerated (EnumType.STRING)
    private WorkMode workMode;

    private LocalDate doj;
    private LocalDate endDate;
    private LocalDate probationEndDate;
    private Integer noticePeriodDay;

    @Enumerated(EnumType.STRING)
    private EmployeeShift shift;

}
