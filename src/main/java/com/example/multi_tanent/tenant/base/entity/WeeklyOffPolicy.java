package com.example.multi_tanent.tenant.base.entity;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "weekly_off_policy")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyOffPolicy {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 50, nullable = false)
    private String code;

    @Column(length = 150, nullable = false)
    private String name;

    @Column(name = "weekday")
    private List<String> offDays; // e.g., ["SUN"], ["FRI","SAT"]

    private boolean rotate = false;

}
