package com.example.multi_tanent.pos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

import com.example.multi_tanent.pos.enums.PosRole;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "pos_users")
public class PosUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store; // optional - user may belong to a specific store

    @Column(nullable = false, unique = true)
    private String email;

    private String displayName;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private PosRole role; // cashier, manager, admin, accountant

    private OffsetDateTime lastLogin;

    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}