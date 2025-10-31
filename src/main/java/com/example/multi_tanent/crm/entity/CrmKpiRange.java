package com.example.multi_tanent.crm.entity;



import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.multi_tanent.spersusers.enitity.Location;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "crm_kpi_ranges")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CrmKpiRange {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "kpi_id", nullable = false)
  @JsonIgnore
  private CrmKpi kpi;

  /** Optional: Makes this range specific to a location */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "location_id")
  private Location location;

  private Double fromPercent;
  private Double toPercent;
  private String color; // hex or name
}
