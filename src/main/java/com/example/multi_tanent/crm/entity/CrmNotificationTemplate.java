
package com.example.multi_tanent.crm.entity;
import com.example.multi_tanent.crm.enums.NotificationEvent;
import com.example.multi_tanent.crm.enums.NotificationModule;
import com.example.multi_tanent.spersusers.enitity.Tenant;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notif_templates",
       uniqueConstraints = @UniqueConstraint(name="uk_template_tenant_mod_event_type",
         columnNames = {"tenant_id","module","event","message_type"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CrmNotificationTemplate {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tenant_id", nullable = false)
  private Tenant tenant;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 60)
  private NotificationModule module;              // Leads, Quotation, KPI…

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 60)
  private NotificationEvent event;                // Add/Update/Delete/Lost/…

  /** Optional sub-type label shown in some screens (Welcome, Event Mail, …). */
  @Column(name = "message_type", length = 80)
  private String messageType;                     // nullable; use when UI shows extra rows

  /** Rich text/HTML content user composes in the editor. */
  @Lob
  @Column(name = "message_body")
  private String messageBody;

  // Channel toggles (checkbox row)
  private boolean bell;
  private boolean email;
  private boolean whatsapp;
  private boolean sms;
  private boolean telegram;

  @Column(name = "screen_popup")
  private boolean screenPopup;

  /** For WhatsApp/SMS providers – “Template/Campaign Id” input in UI. */
  @Column(name = "provider_template_id", length = 120)
  private String providerTemplateId;

  private boolean active = true;

  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  @PrePersist void onCreate() { createdAt = OffsetDateTime.now(); updatedAt = createdAt; }
  @PreUpdate  void onUpdate() { updatedAt = OffsetDateTime.now(); }

  /** “Notification Employee” picker – many users per template. */
  @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CrmNotificationTemplateEmployee> recipients = new ArrayList<>();
}
