package com.example.multi_tanent.config;

import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Properties;

@Component
public class TenantSchemaCreator {
  public void ensureSchema(Object ds) {
    LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
    emf.setDataSource((DataSource) ds);
    emf.setPackagesToScan("com.example.multi_tanent.tenant.entity");
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
}
