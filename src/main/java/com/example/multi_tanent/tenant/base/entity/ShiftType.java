package com.example.multi_tanent.tenant.base.entity;

import java.time.LocalTime;

import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shift_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 50, nullable = false)
    private String code;

    @Column(length = 150, nullable = false)
    private String name;

    private LocalTime startTime;
    private LocalTime endTime;
}
