package com.example.multitenant.config;

import com.example.multitenant.TenantAwareRoutingDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import javax.persistence.EntityManagerFactory;

@Configuration
public class TransactionConfig {
    @Bean
    public PlatformTransactionManager transactionManager(
            EntityManagerFactory emf,
            TenantAwareRoutingDataSource dataSource) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(emf);
        txManager.setDataSource(dataSource);
        return txManager;
    }
}