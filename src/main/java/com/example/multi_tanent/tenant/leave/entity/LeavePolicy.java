package com.example.multi_tanent.tenant.leave.entity;

import com.example.multi_tanent.tenant.base.entity.JobBand;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "leave_policies", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"leave_type_id", "job_band_id"})
})
@Data
public class LeavePolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String policyName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_band_id", nullable = false)
    private JobBand jobBand;

    @Column(nullable = false)
    private Integer allocatedDays;

    private Boolean active = true;
}