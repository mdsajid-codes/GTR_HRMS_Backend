package com.example.multi_tanent.tenant.leave.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "leave_types")
@Data
public class LeaveType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String leaveType;

    private String description;

    @Column(nullable = false)
    private Boolean isPaid;

    private Integer maxDaysPerYear;
}