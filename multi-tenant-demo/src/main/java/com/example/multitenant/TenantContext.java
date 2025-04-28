package com.example.multitenant;

public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static String getCurrentTenantId() {
        return currentTenant.get();
    }

    public static void setCurrentTenantId(String tenantId) {
        currentTenant.set(tenantId);
    }

    public static void clear() {
        currentTenant.remove();
    }
}