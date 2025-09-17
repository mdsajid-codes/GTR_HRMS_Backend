package com.example.multi_tanent.tenant.employee.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employment_policies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmploymentPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer probationDays; // e.g., 90

    private Integer noticePeriodDays; // e.g., 30

    private Boolean autoConfirmAfterProbation;

    private String confirmationCriteria; // text or JSON describing criteria
}
