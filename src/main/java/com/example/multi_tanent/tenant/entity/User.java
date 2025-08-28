package com.example.multi_tanent.tenant.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import com.example.multi_tanent.tenant.entity.enums.Role;

@Entity
@Table(name = "users") // avoid keyword 'user'
@Getter
@Setter
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable=false)
  private String name;

  @Column(nullable=false, unique=true)
  private String email;

  @Column(nullable=false)
  private String passwordHash;

  @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
  @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private Set<Role> roles;

  private Boolean isActive;
  private Boolean isLocked;
  private Integer loginAttempts;
  private LocalDateTime lastLoginAt;
  private String lastLoginIp;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
  private Employee employee;
}