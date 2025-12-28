package com.example.mcpregistry.registry.controller;

import com.example.mcpregistry.registry.service.SidecarDiscoveryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/registry")
public class RegistryController {

    private final SidecarDiscoveryService discoveryService;

    public RegistryController(SidecarDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @GetMapping("/sidecars")
    public Map<String, Object> getSidecars() {
        return Map.of(
                "status", "UP",
                "sidecars", discoveryService.getSidecars());
    }
}
