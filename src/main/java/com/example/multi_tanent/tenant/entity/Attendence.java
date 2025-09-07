package com.example.multi_tanent.tenant.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.multi_tanent.tenant.entity.enums.AttendanceStatus;
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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "attendences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attendence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "employee_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Employee employee;

    private LocalDate attendenceDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    @Enumerated(EnumType.STRING)
    private WorkMode workMode;

    private Double totalHours;
    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
