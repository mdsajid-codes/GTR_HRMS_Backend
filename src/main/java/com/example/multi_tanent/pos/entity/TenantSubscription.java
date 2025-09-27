package com.example.multi_tanent.pos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tenant_subscriptions")
public class TenantSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(nullable = false)
    private String status = "active"; // active, trialing, past_due, canceled

    private OffsetDateTime startedAt;

    private OffsetDateTime currentPeriodStart;

    private OffsetDateTime currentPeriodEnd;

    private OffsetDateTime nextBillingAt;

    private Long quantity = 1L; // seats / multiplicity

    private String providerSubscriptionId; // e.g., Stripe subscription id

    private boolean cancelAtPeriodEnd = false;

    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (startedAt == null) startedAt = OffsetDateTime.now();
    }
}