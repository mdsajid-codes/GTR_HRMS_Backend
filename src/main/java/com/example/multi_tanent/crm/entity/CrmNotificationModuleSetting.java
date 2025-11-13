
package com.example.multi_tanent.crm.entity;

import com.example.multi_tanent.crm.enums.NotificationModule;
import com.example.multi_tanent.spersusers.enitity.Tenant;

import jakarta.persistence.*;
import lombok.*;

/** Per-module switches like “Enable Meta Response”. */
@Entity
@Table(name = "notif_module_settings",
       uniqueConstraints = @UniqueConstraint(name="uk_mod_setting_tenant_module",
         columnNames = {"tenant_id","module"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CrmNotificationModuleSetting {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tenant_id", nullable = false)
  private Tenant tenant;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 60)
  private NotificationModule module;

  /** Specific to Meta Response page (toggle). Safe to reuse for other module flags too. */
  private boolean metaResponseEnabled = false;
}
