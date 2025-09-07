package com.example.multi_tanent.tenant.entity;

import com.example.multi_tanent.tenant.entity.enums.ComponentType;
import com.example.multi_tanent.tenant.entity.enums.TaxTreatment;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"package_id", "componentType"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompensationComponents {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "package_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SalaryDetails salaryDetails;

    @Enumerated(EnumType.STRING)
    private ComponentType componentType;

    private Double amount;
    private Double percentOfBasic;

    @Enumerated(EnumType.STRING)
    private TaxTreatment taxTreatment;

    
}
