package com.example.multi_tanent.tenant.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import com.example.multi_tanent.tenant.entity.enums.EmployeeStatus;
import com.example.multi_tanent.tenant.entity.enums.Gender;
import com.example.multi_tanent.tenant.entity.enums.MartialStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NamedEntityGraph(
  name = "graph.Employee.full",
  attributeNodes = {
    @NamedAttributeNode("user"),
    @NamedAttributeNode("jobDetails"),
    @NamedAttributeNode("jobFillings"),
    @NamedAttributeNode(value = "salaryDetails", subgraph = "subgraph.salaryDetails")
  },
  subgraphs = {
    @NamedSubgraph(name = "subgraph.salaryDetails", attributeNodes = { @NamedAttributeNode("compensationComponents"), @NamedAttributeNode("bankDetails") })
  }
)
public class Employee {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional=false)
  @JoinColumn(name = "user_id", nullable=false, unique=true)
  @JsonIgnore
  private User user; // relational with users (1:1)

  //Basic Details
  @Column(unique = true)
  private String employeeCode;

  private String firstName;
  private String middleName;
  private String lastName;
  private String emailWork;
  private String emailPersonal;
  private String phonePrimary;
  private String phoneSecondary;
  private LocalDate dob;
  
  @Enumerated(EnumType.STRING)
  private Gender gender;

  @Enumerated(EnumType.STRING)
  private MartialStatus martialStatus;

  private String currentAddress;
  private String permanentAddress;
  private String nationalIdType;
  private String nationalIdNumber;

  @Enumerated(EnumType.STRING)
  private EmployeeStatus status;

  private LocalDateTime createdAt;
  private String createdBy;
  private LocalDateTime updatedAt;
  private String updatedBy;

  @OneToMany (mappedBy = "employee", cascade = CascadeType.ALL)
  private Set<JobDetails> jobDetails;

  @OneToMany (mappedBy = "employee", cascade = CascadeType.ALL)
  private Set<JobFilling> jobFillings;

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
  private Set<SalaryDetails> salaryDetails;

}
