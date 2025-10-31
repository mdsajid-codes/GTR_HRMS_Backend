package com.example.multi_tanent.production.entity;

import com.example.multi_tanent.spersusers.enitity.Location;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "pro_work_groups")
public class ProWorkGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id") // Optional relationship
    private Location location;

    @Column(nullable = false, unique = true, updatable = false)
    private String number;

    @Column(nullable = false)
    private String name;

    private String designation;

    @Column(nullable = false)
    private Integer numberOfEmployees = 0;

    @Column(nullable = false)
    private Integer instanceCount = 0;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    private Integer fixedWorkingMinutes;

    @Column(nullable = false)
    private boolean customWorkingHours = false;

    @Column(length = 7) // For hex color codes like #RRGGBB
    private String colorHex;

    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "proworkgroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProWorkGroupDaySchedule> daySchedules;

    @PrePersist
    protected void onCreate() {
        if (this.number == null) {
            this.number = "WG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        this.createdAt = OffsetDateTime.now();
    }
}
