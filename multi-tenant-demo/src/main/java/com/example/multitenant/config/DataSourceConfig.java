package com.example.multitenant.config;

import com.example.multitenant.TenantAwareRoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(TenantAwareRoutingDataSource routingDataSource) {
        return routingDataSource;
    }

    @Bean
    public TenantAwareRoutingDataSource routingDataSource(
            @Qualifier("masterDataSource") DataSource masterDataSource) {
        TenantAwareRoutingDataSource ds = new TenantAwareRoutingDataSource();
        ds.setDefaultTargetDataSource(masterDataSource);
        return ds;
    }

    @Bean(name = "masterDataSource")
    public DataSource masterDataSource(DataSourceProperties properties) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(properties.getUrl());
        ds.setUsername(properties.getUsername());
        ds.setPassword(properties.getPassword());
        return ds;
    }
}