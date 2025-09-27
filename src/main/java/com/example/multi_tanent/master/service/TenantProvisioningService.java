// com/example/multi_tanent/master/service/TenantProvisioningService.java
package com.example.multi_tanent.master.service;

import com.example.multi_tanent.config.TenantRegistry;
import com.example.multi_tanent.config.TenantSchemaCreator;
import com.example.multi_tanent.master.dto.ProvisionTenantRequest;
import com.example.multi_tanent.master.entity.MasterTenant;
import com.example.multi_tanent.master.enums.Role;
import com.example.multi_tanent.master.repository.MasterTenantRepository; // Keep this line
import com.example.multi_tanent.pos.entity.PosUser;
import com.example.multi_tanent.pos.enums.PosRole;
import com.example.multi_tanent.tenant.base.entity.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class TenantProvisioningService {

    private final JdbcTemplate masterJdbc;                 // uses masterDataSource
    private final MasterTenantRepository masterRepo;
    private final TenantRegistry registry;
    private final TenantSchemaCreator schemaCreator;
    private final PasswordEncoder passwordEncoder;

    // use ONE MySQL user that has CREATE DATABASE privilege for provisioning
    // e.g., same credentials that your tenants will use
    @Value("${provisioning.datasource.host}")
    private String mysqlHost;
    @Value("${provisioning.datasource.username}")
    private String mysqlUser;
    @Value("${provisioning.datasource.password}")
    private String mysqlPass;

    public TenantProvisioningService(
            DataSource masterDataSource,
            MasterTenantRepository masterRepo,
            TenantRegistry registry,
            TenantSchemaCreator schemaCreator,
            PasswordEncoder passwordEncoder
    ) {
        this.masterJdbc = new JdbcTemplate(masterDataSource);
        this.masterRepo = masterRepo;
        this.registry = registry;
        this.schemaCreator = schemaCreator;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void provision(ProvisionTenantRequest req) {
        // 1) Validate & normalize tenantId -> safe schema name like tenant_<id>
        String tenantId = normalizeTenantId(req.tenantId());
        String dbName   = "tenant_" + tenantId; // final DB name

        // 2) CREATE DATABASE (needs MySQL user with CREATE privilege)
        //   NOTE: backtick-quote the dbName & ensure it's safe beforehand
        masterJdbc.execute("CREATE DATABASE IF NOT EXISTS `" + dbName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");

        // 3) Save to master_tenant
        String jdbcUrl = "jdbc:mysql://" + mysqlHost + ":3306/" + dbName;
        MasterTenant mt = new MasterTenant();
        mt.setTenantId(tenantId);
        mt.setCompanyName(req.companyName());
        mt.setJdbcUrl(jdbcUrl);
        mt.setUsername(mysqlUser);
        mt.setPassword(mysqlPass);
        mt.setPlan(req.plan());
        masterRepo.save(mt);

        // 4) Add/refresh DataSource in routing map
        registry.addOrUpdateTenant(mt);
        // 5) Ensure schema (tables) for this tenant - This is now handled by registry.addOrUpdateTenant()
        DataSource tenantDs = registry.asTargetMap().get(tenantId) instanceof DataSource d ? d : null;
        if (tenantDs == null) throw new IllegalStateException("Tenant DS not attached");

        // 6) Create schema and seed initial admin users based on the plan
        createSchemaAndSeedData(tenantDs, mt, req);
    }

    private String normalizeTenantId(String raw) {
        if (raw == null) throw new IllegalArgumentException("tenantId required");
        String id = raw.trim().toLowerCase();
        // only allow [a-z0-9_], replace others with _
        id = id.replaceAll("[^a-z0-9_]", "_");
        // basic guard
        if (!Pattern.matches("[a-z0-9_]{3,64}", id)) {
            throw new IllegalArgumentException("Invalid tenantId (use 3-64 of a-z,0-9,_)");
        }
        return id;
    }

    private void createSchemaAndSeedData(DataSource tenantDs, MasterTenant masterTenant, ProvisionTenantRequest req) {
        // Build a temporary EMF bound to THIS tenant DS to write seed data
        LocalContainerEntityManagerFactoryBean emfBean = new LocalContainerEntityManagerFactoryBean();
        emfBean.setDataSource(tenantDs);
        // We need all packages relevant to the plan to correctly build entity mappings
        emfBean.setPackagesToScan(req.plan().getEntityPackages());
        emfBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Properties p = new Properties();
        p.put("hibernate.hbm2ddl.auto", "create"); // Use "create" for a new tenant DB to ensure correct schema generation
        p.put("hibernate.boot.allow_jdbc_metadata_access", "false");
        p.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        p.put("hibernate.show_sql", "true");
        p.put("hibernate.format_sql", "true");
        emfBean.setJpaProperties(p);
        emfBean.afterPropertiesSet();

        EntityManagerFactory emf = emfBean.getObject();
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            java.util.List<String> entityPackages = Arrays.asList(req.plan().getEntityPackages());

            // Seed HRMS Admin User if plan includes HRMS service
            if (entityPackages.contains("com.example.multi_tanent.tenant.base.entity")) {
                User admin = new User();
                admin.setName("Tenant Admin");
                admin.setEmail(req.adminEmail());
                admin.setPasswordHash(passwordEncoder.encode(req.adminPassword()));
                admin.setPlan(req.plan());
                admin.setRoles(Set.of(Role.TENANT_ADMIN));
                admin.setIsActive(true);
                admin.setIsLocked(false);
                admin.setLastLoginAt(LocalDateTime.now());
                admin.setCreatedAt(LocalDateTime.now());
                admin.setUpdatedAt(LocalDateTime.now());
                admin.setLoginAttempts(0);
                em.persist(admin);
            }

            // Seed POS Admin User if plan includes POS service
            if (entityPackages.contains("com.example.multi_tanent.pos.entity")) {
                // First, create the Tenant record for the POS module
                com.example.multi_tanent.pos.entity.Tenant posTenant = new com.example.multi_tanent.pos.entity.Tenant();
                posTenant.setName(masterTenant.getCompanyName());
                em.persist(posTenant);

                // Now, create the POS admin user
                PosUser posAdmin = PosUser.builder()
                        .tenant(posTenant)
                        .email(req.adminEmail()) // Using email as username
                        .displayName("POS Administrator")
                        .passwordHash(passwordEncoder.encode(req.adminPassword()))
                        .role(PosRole.POS_ADMIN)
                        .build();
                em.persist(posAdmin);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            // Re-throw to be handled by the main transactional method, which will cause a rollback
            throw new RuntimeException("Failed to seed initial tenant data", e);
        } finally {
            emfBean.destroy(); // This also closes the EntityManagerFactory
        }
    }
}
