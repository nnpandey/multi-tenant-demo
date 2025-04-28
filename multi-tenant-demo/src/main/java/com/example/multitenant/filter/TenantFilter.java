package com.example.multitenant.filter;

import com.example.multitenant.TenantAwareRoutingDataSource;
import com.example.multitenant.TenantContext;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;

@Component
@Order(0)
public class TenantFilter extends OncePerRequestFilter {

    @Autowired
    private TenantAwareRoutingDataSource dataSource; // inject the routing DS

    @Autowired
    Environment environment;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/api/tenants")) {
            chain.doFilter(request, response);
            return;
        }

        String tenantId = request.getHeader("X-Tenant-ID");
        if (tenantId == null || tenantId.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "X-Tenant-ID header is required");
            return;
        }

        try {
            TenantContext.setCurrentTenantId(tenantId);

            // Register tenant DB dynamically if not present
            if (!dataSource.hasDataSource(tenantId)) {
                dataSource.addDataSource(tenantId, createTenantDataSource(tenantId));
            }

            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private DataSource createTenantDataSource(String tenantId) {
        HikariDataSource ds = new HikariDataSource();
        tenantId = tenantId.trim().replace("\"", "");
        ds.setJdbcUrl(environment.getProperty("app.db.admin-url") +"tenant_" + tenantId); // change if needed
        ds.setUsername(environment.getProperty("spring.datasource.username"));
        ds.setPassword(environment.getProperty("spring.datasource.password"));
        ds.setDriverClassName(environment.getProperty("spring.datasource.driver-class-name"));
        return ds;
    }
}
