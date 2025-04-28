package com.example.multitenant;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class TenantAwareRoutingDataSource extends AbstractRoutingDataSource {
    private final Map<Object, Object> targetDataSources = new HashMap<>();

    @PostConstruct
    public void init() {
        setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            return "master";
        }
        return tenantId;
    }

    public synchronized void addDataSource(String tenantId, DataSource dataSource) {
        targetDataSources.put(tenantId, dataSource);
        setTargetDataSources(new HashMap<>(targetDataSources)); // this is essential
        super.afterPropertiesSet(); // refresh routing
    }

    public boolean hasDataSource(String tenantId) {
        return targetDataSources.containsKey(tenantId);
    }



}