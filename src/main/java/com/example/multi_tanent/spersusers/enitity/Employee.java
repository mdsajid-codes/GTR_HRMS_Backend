package com.example.multi_tanent.spersusers.enitity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.multi_tanent.spersusers.enums.EmployeeStatus;
import com.example.multi_tanent.spersusers.enums.Gender;
import com.example.multi_tanent.spersusers.enums.MartialStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Entity
@Table(name = "employees")
@Getter
@Setter
@ToString(exclude = {"user", "location"})
@EqualsAndHashCode(exclude = {"user", "location"})
public class Employee {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional=false)
  @JoinColumn(name = "user_id", nullable=false, unique=true)
  @JsonBackReference("user-employee")
  private User user; // relational with users (1:1)

  @ManyToOne
  @JoinColumn(name = "location_id")
  private Location location; // Primary work location

  //Basic Details
  @Column(unique = true)
  private String employeeCode;

  private String firstName;
  private String middleName;
  private String lastName;
  private String emailWork;
  private String emailPersonal;
  private String phonePrimary;
  private LocalDate dob;
  
  @Enumerated(EnumType.STRING)
  private Gender gender;

  @Enumerated(EnumType.STRING)
  private MartialStatus martialStatus;

  @Enumerated(EnumType.STRING)
  private EmployeeStatus status;

  private String photoPath;

  private LocalDateTime createdAt;
  private String createdBy;
  private LocalDateTime updatedAt;
  private String updatedBy;

}
