package com.example.multitenant.service;

import com.example.multitenant.entity.Tenant;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TenantDataSourceManager {
    private final Map<String, DataSource> tenantDataSources = new ConcurrentHashMap<>();
    private final DataSourceProperties dataSourceProperties;

    public TenantDataSourceManager(DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    public void addTenantDataSource(Tenant tenant) {
        if (!tenantDataSources.containsKey(tenant.getDatabaseName())) {
            DataSource ds = createTenantDataSource(tenant);
            tenantDataSources.put(tenant.getDatabaseName(), ds);
        }
    }

    private DataSource createTenantDataSource(Tenant tenant) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dataSourceProperties.getUrl()
                .replaceFirst("/[^/]+$", "/" + tenant.getDatabaseName()));
        config.setUsername(tenant.getDbUsername());
        config.setPassword(tenant.getDbPassword());
        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);
    }
}