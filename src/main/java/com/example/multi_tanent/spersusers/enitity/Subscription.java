package com.example.multi_tanent.spersusers.enitity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table (name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer numberOfLocations;
    private Integer numberOfUsers;
    private Integer hrmsAccessCount;
    private Integer numberOfStore;
    private LocalDate subscriptionStartDate;
    private LocalDate subscriptionEndDate;
}
