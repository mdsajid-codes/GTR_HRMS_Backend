package com.example.multi_tanent.pos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

import com.example.multi_tanent.spersusers.enitity.Tenant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    private String name;

    private String phone;

    private String email;

    private Long loyaltyPoints = 0L;

    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}