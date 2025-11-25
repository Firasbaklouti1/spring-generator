package com.firas.generator.model;

import java.util.List;

public class ProjectRequest {
    private String groupId;
    private String artifactId;
    private String name;
    private String description;
    private String packageName;
    private String javaVersion;
    private String bootVersion;
    private List<String> dependencies;
    
    // Advanced features
    private boolean includeEntity;
    private boolean includeRepository;
    private boolean includeService;
    private boolean includeController;
    private boolean includeDto;
    private boolean includeMapper;
    
    // SQL content for auto-generation
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
