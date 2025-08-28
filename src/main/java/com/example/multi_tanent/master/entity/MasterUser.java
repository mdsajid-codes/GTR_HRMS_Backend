package com.example.multi_tanent.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "master_user", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
@Getter
@Setter
public class MasterUser {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable=false, unique=true)
  private String username;

  @Column(nullable=false)
  private String passwordHash; // BCrypt

  @Column(nullable=false)
  private boolean superAdmin = true;
  // getters/setters
}
