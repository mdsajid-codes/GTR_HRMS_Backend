package com.example.multi_tanent.tenant.entity;

import java.time.LocalDate;

import com.example.multi_tanent.tenant.entity.enums.BackgroundStatus;
import com.example.multi_tanent.tenant.entity.enums.HiringSource;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table (name = "job_fillings")
@NoArgsConstructor
@Data
@AllArgsConstructor
public class JobFilling {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private HiringSource hiringSource;

    private LocalDate offerDate;
    private LocalDate offerAcceptedDate;
    private LocalDate joiningDate;

    @Enumerated(EnumType.STRING)
    private BackgroundStatus backgroundStatus;
}
