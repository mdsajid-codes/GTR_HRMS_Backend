package com.example.multi_tanent.crm.entity;

import com.example.multi_tanent.spersusers.enitity.Employee;
import com.example.multi_tanent.spersusers.enitity.Tenant;
//import com.example.multi_tanent.crm.entity.Employee; // or your actual Employee package
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notif_template_employees",
       uniqueConstraints = @UniqueConstraint(name="uk_template_employee",
         columnNames = {"template_id","employee_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CrmNotificationTemplateEmployee {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "template_id", nullable = false)
  private CrmNotificationTemplate template;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "employee_id", nullable = false)
  private Employee employee;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tenant_id", nullable = false)
  private Tenant tenant;
}
