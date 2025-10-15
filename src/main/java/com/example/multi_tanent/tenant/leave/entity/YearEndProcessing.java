package com.example.multi_tanent.tenant.leave.entity;

import com.example.multi_tanent.tenant.leave.enums.NegativeBalancePolicy;
import com.example.multi_tanent.tenant.leave.enums.YearEndAction;
import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class YearEndProcessing {

    /** Positive balance handling */
    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private YearEndAction positiveBalanceAction; // EXPIRE_OR_RESET, PAY_OUT, CARRY_FORWARD, PAY_THEN_CARRY, CARRY_THEN_PAY

    /** Carry-forward expiry (only when carry-forward is used by policy logic) */
    private Boolean carryForwardExpires;     // null/false = no expiry
    private Integer carryForwardExpiryDays;  // days in next leave year

    /** Negative balance handling */
    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private NegativeBalancePolicy negativeBalancePolicy; // DEDUCT_FROM_SALARY / NULLIFY / CARRY_FORWARD_NEGATIVE
}