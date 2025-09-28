package com.example.multi_tanent.master.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

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

  // Store selected modules as a JSON array of strings in the database
  @Type(JsonType.class)
  @Column(columnDefinition = "json")
  private java.util.List<ServiceModule> serviceModules;

  public String[] getEntityPackages() {
    return ServiceModule.getPackagesForModules(this.serviceModules);
  }

  // getters/setters
}
