package com.firas.generator.controller;

import com.firas.generator.model.DependencyGroup;
import com.firas.generator.service.DependencyRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for managing Spring Boot dependencies.
 * 
 * This controller provides endpoints to retrieve available Spring Boot dependencies
 * grouped by categories (e.g., Web, Security, Database, etc.). The dependencies are
 * fetched dynamically from the Spring Initializr API.
 * 
 * @author Firas Baklouti
 * @version 1.0
 * @since 2025-12-01
 */
@RestController
@RequestMapping("/api/dependencies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DependencyController {

    /**
     * Service for managing and retrieving dependency metadata
     */
    private final DependencyRegistry dependencyRegistry;

    /**
     * Retrieves all available dependency groups with their associated dependencies.
     * 
     * Each group contains related dependencies (e.g., all web-related dependencies
     * are grouped together). This endpoint is typically used to populate dependency
     * selection UI in the frontend.
     * 
     * @return List of dependency groups, each containing multiple dependencies
     */
    @GetMapping("/groups")
    public List<DependencyGroup> getDependencyGroups() {
        return dependencyRegistry.getAllGroups();
    }
}
