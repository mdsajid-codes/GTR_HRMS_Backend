package com.example.multi_tanent.tenant.leave.entity;

import com.example.multi_tanent.tenant.leave.enums.SandwichPolicy;
import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SandwichRules {

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private SandwichPolicy holidayAdjacencyPolicy;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private SandwichPolicy weekOffAdjacencyPolicy;
}