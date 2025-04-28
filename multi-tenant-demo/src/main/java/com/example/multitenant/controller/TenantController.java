package com.example.multitenant.controller;

import com.example.multitenant.entity.Tenant;
import com.example.multitenant.service.DatabaseProvisioningService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final DatabaseProvisioningService provisioningService;

    public TenantController(DatabaseProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Tenant createTenant(@RequestParam String name) {
        return provisioningService.createTenant(name);
    }
}