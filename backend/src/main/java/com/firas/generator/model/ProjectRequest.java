package com.firas.generator.model;

import java.util.List;

/**
 * Represents a request to generate a Spring Boot project.
 * 
 * This class encapsulates all the configuration options needed to generate a customized
 * Spring Boot project, including basic project metadata, dependency selections, and
 * advanced code generation options such as automatic CRUD generation from SQL schemas.
 * 
 * @author Firas Baklouti
 * @version 1.0
 * @since 2025-12-01
 */
public class ProjectRequest {
    /** Maven groupId for the generated project (e.g., "com.example") */
    private String groupId;
    
    /** Maven artifactId for the generated project (e.g., "demo") */
    private String artifactId;
    
    /** Human-readable project name */
    private String name;
    
    /** Project description */
    private String description;
    
    /** Base package name for Java classes (e.g., "com.example.demo") */
    private String packageName;
    
    /** Java version to use (e.g., "17", "21") */
    private String javaVersion;
    
    /** Spring Boot version to use (e.g., "3.2.0") */
    private String bootVersion;
    
    /** List of dependency IDs to include in the project */
    private List<String> dependencies;
    
    // Advanced features for code generation
    
    /** Flag to include JPA entity classes in generated code */
    private boolean includeEntity;
    
    /** Flag to include Spring Data repository interfaces */
    private boolean includeRepository;
    
    /** Flag to include service layer classes */
    private boolean includeService;
    
    /** Flag to include REST controller classes */
    private boolean includeController;
    
    /** Flag to include DTO (Data Transfer Object) classes */
    private boolean includeDto;
    
    /** Flag to include mapper classes for entity-DTO conversion */
    private boolean includeMapper;
    
    /** SQL schema content for automatic CRUD generation */
    private String sqlContent;

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getArtifactId() { return artifactId; }
    public void setArtifactId(String artifactId) { this.artifactId = artifactId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getJavaVersion() { return javaVersion; }
    public void setJavaVersion(String javaVersion) { this.javaVersion = javaVersion; }

    public String getBootVersion() { return bootVersion; }
    public void setBootVersion(String bootVersion) { this.bootVersion = bootVersion; }

    public List<String> getDependencies() { return dependencies; }
    public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }

    public boolean isIncludeEntity() { return includeEntity; }
    public void setIncludeEntity(boolean includeEntity) { this.includeEntity = includeEntity; }

    public boolean isIncludeRepository() { return includeRepository; }
    public void setIncludeRepository(boolean includeRepository) { this.includeRepository = includeRepository; }

    public boolean isIncludeService() { return includeService; }
    public void setIncludeService(boolean includeService) { this.includeService = includeService; }

    public boolean isIncludeController() { return includeController; }
    public void setIncludeController(boolean includeController) { this.includeController = includeController; }

    public boolean isIncludeDto() { return includeDto; }
    public void setIncludeDto(boolean includeDto) { this.includeDto = includeDto; }

    public boolean isIncludeMapper() { return includeMapper; }
    public void setIncludeMapper(boolean includeMapper) { this.includeMapper = includeMapper; }

    public String getSqlContent() { return sqlContent; }
    public void setSqlContent(String sqlContent) { this.sqlContent = sqlContent; }
}
