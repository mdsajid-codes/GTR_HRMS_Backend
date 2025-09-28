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
@Table(name = "stores")
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(length = 3)
    private String currency = "AED"; // Dubai-based default

    private String timezone = "Asia/Dubai";

    private String vatNumber;

    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}