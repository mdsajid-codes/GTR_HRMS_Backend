package com.example.multi_tanent.tenant.leave.entity;

import com.example.multi_tanent.tenant.leave.enums.ApproverSelectionMode;
import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalFlow {

    /** Does this leave type require approval? */
    private boolean approvalRequired;

    /** Optional summary (if using dynamic chain below, this can be informational) */
    private Integer levels;

    /** Primary selection mode hint */
    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private ApproverSelectionMode selectionMode; // ROLE_BASED / NAMED_EMPLOYEES
}