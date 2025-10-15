package com.example.multi_tanent.tenant.leave.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;
import java.util.HashSet;

import com.example.multi_tanent.tenant.leave.enums.AccrualInterval;
import com.example.multi_tanent.tenant.leave.enums.AccrualType;
import com.example.multi_tanent.tenant.leave.enums.AnnualQuotaLimitType;
import com.example.multi_tanent.tenant.leave.enums.MidYearJoinProratePolicy;
import com.example.multi_tanent.tenant.leave.enums.RoundingPolicy;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "leave_type_policies",
       uniqueConstraints = @UniqueConstraint(columnNames = {"policy_id", "leave_type_id"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveTypePolicy {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    @JsonIgnore
    private LeavePolicy policy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    @JsonBackReference
    private LeaveType leaveType;

    /* ===== Annual quota options (screens: Limit/Unlimited + prorate + join-month cutoff) ===== */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AnnualQuotaLimitType quotaLimitType;  // LIMITED or UNLIMITED

    /** Only when quotaLimitType = LIMITED */
    private Integer quotaDays; // annual limit

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private MidYearJoinProratePolicy midYearJoinProratePolicy; // PRORATE_ON_JOIN_DATE or FULL_QUOTA

    /** If prorating: “no leave in joining month if joined after Nth” (nullable) */
    private Integer joinMonthCutoffDay;

    /* ===== Accrual & rounding (screens: entire quota vs earned + rounding options) ===== */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AccrualType accrualType; // ENTIRE_QUOTA or PERIODIC

    @Enumerated(EnumType.STRING)
    private AccrualInterval accrualInterval; // nullable unless PERIODIC

    private Double accrualAmountDays; // nullable unless PERIODIC. Use Double for partial days.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RoundingPolicy roundingPolicy;

    /* ===== Step 3: Applying for leave (incl. prior notice, earliest apply, backdated) ===== */
    @Embedded
    private ApplicationSettings applicationSettings;

    /* ===== Step 4: Restrictions (eligibility, consecutive/monthly caps, notice-period) ===== */
    @Embedded
    private RestrictionSettings restrictionSettings;

    /* ===== Step 5: Holidays & Weekoffs (sandwich rules) ===== */
    @Embedded
    private SandwichRules sandwichRules;

    /* ===== Step 6: Approvals ===== */
    @Embedded
    private ApprovalFlow approvalFlow;

    @OneToMany(mappedBy = "leaveTypePolicy", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("levelOrder ASC")
    @Builder.Default
    private Set<ApprovalLevel> approvalLevels = new HashSet<>();

    /* ===== Step 7: Year-end processing ===== */
    @Embedded
    private YearEndProcessing yearEndProcessing;

    /* ===== Annual-leave tenure/pro-rata rules (only for ANNUAL; nullable otherwise) ===== */
    @Embedded
    private AnnualEntitlementRules annualEntitlementRules;
}