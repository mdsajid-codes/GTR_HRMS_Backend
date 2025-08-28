// com/example/multi_tanent/master/controller/TenantProvisioningController.java
package com.example.multi_tanent.master.controller;

import com.example.multi_tanent.master.dto.ProvisionTenantRequest;
import com.example.multi_tanent.master.service.TenantProvisioningService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/master/tenants")
public class TenantProvisioningController {

    private final TenantProvisioningService service;

    public TenantProvisioningController(TenantProvisioningService service) {
        this.service = service;
    }

    @PostMapping("/provision")
    @PreAuthorize("hasRole('MASTER_ADMIN')")
    public String provision(@RequestBody ProvisionTenantRequest req) {
        service.provision(req);
        return "tenant created & admin initialized";
    }
}
