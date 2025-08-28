// com/example/multi_tanent/master/service/TenantProvisioningService.java
package com.example.multi_tanent.master.service;

import com.example.multi_tanent.config.TenantRegistry;
import com.example.multi_tanent.config.TenantSchemaCreator;
import com.example.multi_tanent.master.dto.ProvisionTenantRequest;
import com.example.multi_tanent.master.entity.MasterTenant;
import com.example.multi_tanent.master.repository.MasterTenantRepository;
import com.example.multi_tanent.tenant.entity.User;
import com.example.multi_tanent.tenant.entity.enums.Role;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

import java.time.LocalDateTime;
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
    private final String mysqlHost = "localhost";
    private final String mysqlUser = "root";
    private final String mysqlPass = "Sajid7254@";

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
        masterRepo.save(mt);

        // 4) Add/refresh DataSource in routing map
        registry.addOrUpdateTenant(mt);
        DataSource tenantDs = registry.asTargetMap().get(tenantId) instanceof DataSource d ? d : null;
        if (tenantDs == null) throw new IllegalStateException("Tenant DS not attached");

        // 5) Ensure schema (tables) for this tenant
        schemaCreator.ensureSchema(tenantDs);

        // 6) Seed TENANT_ADMIN user inside the tenant DB
        seedTenantAdmin(tenantDs, req.adminEmail(), req.adminPassword());
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

    private void seedTenantAdmin(DataSource tenantDs, String email, String password) {
        // Build a temporary EMF bound to THIS tenant DS to write seed data
        LocalContainerEntityManagerFactoryBean emfBean = new LocalContainerEntityManagerFactoryBean();
        emfBean.setDataSource(tenantDs);
        emfBean.setPackagesToScan("com.example.multi_tanent.tenant.entity");
        emfBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Properties p = new Properties();
        p.put("hibernate.hbm2ddl.auto", "none"); // schema already ensured above
        p.put("hibernate.boot.allow_jdbc_metadata_access", "false");
        p.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        p.put("hibernate.show_sql", "true");
        p.put("hibernate.format_sql", "true");
        emfBean.setJpaProperties(p);
        emfBean.afterPropertiesSet();

        EntityManagerFactory emf = emfBean.getObject();
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        User admin = new User();
        admin.setName("Tenant Admin");
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRoles(Set.of(Role.TENANT_ADMIN));
        admin.setIsActive(true);
        admin.setIsLocked(false);
        admin.setLastLoginAt(LocalDateTime.now());
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        admin.setLoginAttempts(0);

        em.persist(admin);
        em.getTransaction().commit();
        em.close();
        emfBean.destroy();
    }
}
