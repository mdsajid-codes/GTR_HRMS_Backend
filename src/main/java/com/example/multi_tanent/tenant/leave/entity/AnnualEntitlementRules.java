package com.example.multi_tanent.tenant.leave.entity;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AnnualEntitlementRules {

    /** a) After N years of service => M days (lump-sum entitlement) */
    private Integer minYearsForFullQuota;
    private Integer fullQuotaDays;

    /** b) After N months => K days per month (monthly cap) */
    private Integer monthsForMonthlyCap;
    private Integer daysPerMonthToCap;
}