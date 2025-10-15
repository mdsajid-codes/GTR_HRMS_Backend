package com.example.multi_tanent.tenant.leave.entity;

import com.example.multi_tanent.tenant.leave.enums.WorkingDayRequirementMode;
import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ApplicationSettings {

    /** Half-day allowed? */
    private boolean allowHalfDay;

    /** Can employees self-apply (else HR/Manager applies)? */
    private boolean selfApplyAllowed;

    /** Require non-empty comment? */
    private boolean requireComment;

    /** Require attachment if leave exceeds N calendar days (null = none) */
    private Integer attachmentIfExceedsDays;

    /* ---------- Submission timing rules from UI ---------- */

    /** Prior-notice triggers when requested duration >= thresholdCalendarDays (nullable) */
    private Integer priorNoticeThresholdCalendarDays;

    /** Calendar days of notice required (nullable) */
    private Integer priorNoticeCalendarDays;

    /** Working-day requirement mode & value (nullable) */
    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private WorkingDayRequirementMode workingDayRequirementMode; // NONE / AT_LEAST_N_WORKING_DAYS

    private Integer workingDayRequirementMinDays;

    /** Earliest a request may be submitted before start date (no sooner than N days) */
    private Integer earliestApplyWindowDays;

    /** Back-dated application allowance & max days back (nullable) */
    private Boolean allowBackdatedApplication;
    private Integer backdatedMaxDays;
}