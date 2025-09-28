package com.example.multi_tanent.spersusers.enitity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;

import com.example.multi_tanent.master.enums.Role;
import com.example.multi_tanent.pos.entity.Store;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "users") // avoid keyword 'user'
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"employee", "tenant", "store"})
@EqualsAndHashCode(exclude = {"employee", "tenant", "store"})
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id") // Temporarily nullable for migration
    private Tenant tenant;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(nullable=false)
    private String name;

    @Column(nullable=false, unique=true)
    private String email;

    @Column(nullable=false)
    @JsonIgnore
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

    @OneToOne(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JsonManagedReference("user-employee")
    private Employee employee;

    // Helper methods for bidirectional relationship
    public void setEmployee(Employee employee) {
        if (employee == null) {
            if (this.employee != null) {
                this.employee.setUser(null);
            }
        } else {
            employee.setUser(this);
        }
        this.employee = employee;
    }

    public void removeEmployee() {
        setEmployee(null);
    }
}