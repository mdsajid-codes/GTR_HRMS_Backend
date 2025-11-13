package com.example.multi_tanent.crm.entity;

import com.example.multi_tanent.spersusers.enitity.Location;
import com.example.multi_tanent.spersusers.enitity.Employee;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "crm_companies", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrmCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_type_id")
    private CompanyType companyType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "industry_id")
    private CrmIndustry industry;

    private String phone;
    private String email;
    private String website;

    /** Company Owner Name */
    private String companyOwner;

    /** Parent Company for hierarchies */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_company_id", foreignKey = @ForeignKey(name = "fk_company_parent"))
    private CrmCompany parentCompany;

    /* ---------- Billing Address ---------- */
    private String billingStreet;
    private String billingCity;
    private String billingZip;
    private String billingState;
    private String billingCountry;

    /* ---------- Shipping Address ---------- */
    private String shippingStreet;
    private String shippingCity;
    private String shippingZip;
    private String shippingState;
    private String shippingCountry;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}