package com.firas.generator.model;

public class DependencyMetadata {
    private String id;
    private String name;
    private String description;
    private String groupId;
    private String artifactId;
    private String version;
    private String scope;
    private boolean isStarter;

    public DependencyMetadata() {
    }

    public DependencyMetadata(String id, String name, String description, String groupId, String artifactId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.isStarter = artifactId != null && artifactId.startsWith("spring-boot-starter-");
    }

    public DependencyMetadata(String id, String name, String description, String groupId, String artifactId, String version, String scope) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.scope = scope;
        this.isStarter = artifactId != null && artifactId.startsWith("spring-boot-starter-");
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getArtifactId() { return artifactId; }
    public void setArtifactId(String artifactId) { this.artifactId = artifactId; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public boolean isStarter() { return isStarter; }
    public void setStarter(boolean starter) { isStarter = starter; }
}
