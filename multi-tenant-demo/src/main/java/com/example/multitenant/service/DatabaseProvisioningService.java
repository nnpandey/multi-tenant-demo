package com.example.multitenant.service;

import com.example.multitenant.TenantAwareRoutingDataSource;
import com.example.multitenant.entity.Tenant;
import com.example.multitenant.repository.TenantRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;
import java.util.UUID;

@Service
public class DatabaseProvisioningService {
    @Autowired
    private JdbcTemplate adminJdbcTemplate;
    @Autowired
    private TenantAwareRoutingDataSource routingDataSource;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    TenantRepository tenantRepository;

    @Autowired
    private Environment environment;

    //@Transactional
    public Tenant createTenant(String tenantName) {
        String dbName = "tenant_" + tenantName.toLowerCase();
        String username = environment.getProperty("spring.datasource.username");
        String password = environment.getProperty("spring.datasource.password");

        // 1. Create database
        adminJdbcTemplate.execute("CREATE DATABASE " + dbName);
        /*adminJdbcTemplate.execute("CREATE USER " + username + " WITH PASSWORD '" + password + "'");
        adminJdbcTemplate.execute("GRANT ALL PRIVILEGES ON DATABASE " + dbName + " TO " + username);*/

        // 2. Initialize schema with direct connection
        DataSource tenantDataSource = createDataSource(dbName, username, password);
        initializeSchema(tenantDataSource);

        // 3. Register datasource for runtime use
        routingDataSource.addDataSource(dbName, tenantDataSource);

        // 4. Save tenant
        Tenant tenant = new Tenant();
        tenant.setName(tenantName);
        tenant.setDatabaseName(dbName);
        tenant.setDbUsername(username);
        tenant.setDbPassword(password);
        tenantRepository.save(tenant);
        return tenant;
    }

    private DataSource createDataSource(String dbName, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(environment.getProperty("app.db.admin-url") + dbName);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        return new HikariDataSource(config);
    }

    private void initializeSchema(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.example.multitenant.entity");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties props = new Properties();
        props.put("hibernate.hbm2ddl.auto", "create");
        props.put("hibernate.dialect", environment.getProperty("spring.jpa.properties.hibernate.dialect"));
        factory.setJpaProperties(props);

        factory.afterPropertiesSet();
        try {
            factory.getObject().createEntityManager().close();
        } finally {
            factory.destroy();
        }
    }
}