package com.example.multi_tanent.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "master_tenant", uniqueConstraints = @UniqueConstraint(columnNames = "tenantId"))
@Getter
@Setter
public class MasterTenant {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable=false, unique=true)
  private String tenantId;

  @Column(nullable=false)
  private String companyName;

  @Column(nullable=false)
  private String jdbcUrl;

  @Column(nullable=false)
  private String username;

  @Column(nullable=false)
  private String password;

  // getters/setters
}
