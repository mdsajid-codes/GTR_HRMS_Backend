package com.example.multi_tanent.tenant.leave.entity;

import com.example.multi_tanent.tenant.leave.enums.EligibilityAnchor;
import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RestrictionSettings {

    /* New joiner eligibility */
    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private EligibilityAnchor eligibilityAnchor;  // AFTER_PROBATION / AFTER_JOINING
    private Integer eligibilityAfterDays;

    /* Consecutive days cap */
    private boolean limitConsecutiveDays;
    private Integer maxConsecutiveDays;

    /* Monthly usage cap */
    private boolean limitMonthlyUsage;
    private Integer maxMonthlyDays;

    /* Allowed during notice period? */
    private boolean allowedInNoticePeriod;

    /* Request throttling */
    private Integer minGapBetweenRequestsDays; // null = none
    private Integer maxRequestsPerYear;        // null = unlimited
    private Integer maxRequestsPerMonth;       // null = unlimited
}