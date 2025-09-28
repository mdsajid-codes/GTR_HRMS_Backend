package com.example.multi_tanent.tenant.employee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.example.multi_tanent.spersusers.enitity.Employee;
import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;

@Entity
@Table (name = "employee_profile")
@Getter
@Setter
@ToString(exclude = "employee")
@EqualsAndHashCode(exclude = "employee")
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeProfile {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    @JsonBackReference
    private Employee employee;

    @Column (length = 1000)
    private String address;

    private String city;
    private String state;
    private String country;

    @Column(name = "postal_code", length = 30)
    private String postalCode;

    private String emergencyContactName;
    private String emergencyContactRelation;
    private String emergencyContactPhone;
    private String bankName;
    private String bankAccountNumber;
    private String ifscCode;
    private String bloodGroup;

    @Column(length = 2000)
    private String notes;

}
