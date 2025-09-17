package com.example.multi_tanent.config;

import com.example.multi_tanent.master.entity.TenantPlan;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Properties;

@Component
public class TenantSchemaCreator {
  public void ensureSchema(DataSource ds, TenantPlan plan) {
    if (plan == null) {
      System.err.println("WARNING: No plan specified for tenant. Skipping schema creation.");
      return;
    }
    LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
    emf.setDataSource(ds);
    emf.setPackagesToScan(plan.getEntityPackages());
    emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    Properties props = new Properties();
    props.put("hibernate.hbm2ddl.auto", "update");
    props.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
    props.put("hibernate.show_sql", "true");
    props.put("hibernate.format_sql", "true");
    emf.setJpaProperties(props);
    emf.afterPropertiesSet();
    emf.destroy();
  }

  public void ensureSchema(Object object) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'ensureSchema'");
  }
}
