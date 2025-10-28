package com.example.multi_tanent.production.entity;

import com.example.multi_tanent.spersusers.enitity.Employee;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "pro_work_stations")
public class ProWorkStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "workgroup_id", nullable = false)
    private ProWorkGroup workGroup;

    @Column(nullable = false, unique = true, updatable = false)
    private String workstationNumber;

    @Column(nullable = false)
    private String workstationName;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "pro_workstation_employees",
            joinColumns = @JoinColumn(name = "workstation_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id"))
    private List<Employee> employees = new ArrayList<>();

    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.workstationNumber == null) {
            this.workstationNumber = "WS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        this.createdAt = OffsetDateTime.now();
    }
}
