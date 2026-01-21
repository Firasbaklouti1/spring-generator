package com.firas.generator.model.config;

/**
 * Enum representing the available project structure formats for Spring Boot projects.
 * 
 * Each structure defines how source files are organized in the generated project:
 * - LAYERED: Traditional folder-by-type organization (entity/, repository/, service/, controller/)
 * - FEATURE: Folder-by-feature organization (user/User.java, user/UserRepository.java, etc.)
 * - DDD: Domain-Driven Design organization (domain/user/entity/, domain/user/repository/)
 * - HEXAGONAL: Hexagonal/Clean Architecture (domain/model/, application/port/, infrastructure/adapter/)
 * 
 * @author Firas Baklouti
 * @version 1.0
 * @since 2025-01-21
 */
public enum ProjectStructure {
    
    /**
     * Traditional layered/folder-by-type structure.
     * All entities in entity/, all repositories in repository/, etc.
     * Example paths:
     * - entity/User.java
     * - repository/UserRepository.java
     * - service/UserService.java
     * - controller/UserController.java
     */
    LAYERED("layered", "Layered (Package by Type)", 
            "Traditional structure with separate folders for each layer"),
    
    /**
     * Feature/domain-based structure.
     * Each feature/entity has its own package containing all related classes.
     * Example paths:
     * - user/User.java
     * - user/UserRepository.java
     * - user/UserService.java
     * - user/UserController.java
     */
    FEATURE("feature", "Feature (Package by Feature)", 
            "Each feature has its own package with all related classes"),
    
    /**
     * Domain-Driven Design structure.
     * Organized by bounded contexts with domain, application, and infrastructure layers.
     * Example paths:
     * - domain/user/entity/User.java
     * - domain/user/repository/UserRepository.java
     * - domain/user/service/UserService.java
     * - application/controller/UserController.java
     */
    DDD("ddd", "DDD (Domain-Driven Design)", 
        "Organized by bounded contexts with domain-focused structure"),
    
    /**
     * Hexagonal/Clean Architecture structure.
     * Separates domain, application ports, and infrastructure adapters.
     * Example paths:
     * - domain/model/User.java
     * - application/port/in/UserUseCase.java
     * - application/port/out/UserRepositoryPort.java
     * - infrastructure/adapter/in/web/UserController.java
     * - infrastructure/adapter/out/persistence/UserRepository.java
     */
    HEXAGONAL("hexagonal", "Hexagonal (Clean Architecture)", 
              "Ports and adapters architecture with clear separation of concerns");
    
    /** Unique identifier for the structure (used in API requests) */
    private final String id;
    
    /** Human-readable display name */
    private final String displayName;
    
    /** Description of the structure */
    private final String description;
    
    /**
     * Construct a ProjectStructure enum constant with the provided identifier, display name, and description.
     *
     * @param id          unique identifier for the structure (used in API requests)
     * @param displayName human-readable name for UI/display
     * @param description textual description of the structure
     */
    ProjectStructure(String id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
 * Gets the identifier for this project structure.
 *
 * @return the identifier string for this project structure, used in API requests (e.g., "layered")
 */
public String getId() { return id; }
    /**
 * Human-readable name for the project structure used in user interfaces.
 *
 * @return the display name of the project structure
 */
public String getDisplayName() { return displayName; }
    /**
 * Human-readable description of this project structure.
 *
 * @return the description text for this project structure
 */
public String getDescription() { return description; }
    
    /**
     * Find a ProjectStructure by its ID (case-insensitive).
     * 
     * @param id The structure identifier
     * @return The matching ProjectStructure, or LAYERED if not found
     */
    public static ProjectStructure fromId(String id) {
        if (id == null) return LAYERED;
        for (ProjectStructure structure : values()) {
            if (structure.id.equalsIgnoreCase(id)) {
                return structure;
            }
        }
        return LAYERED; // Default to LAYERED for backward compatibility
    }
}