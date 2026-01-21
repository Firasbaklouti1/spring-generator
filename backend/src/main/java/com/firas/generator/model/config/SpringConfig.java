package com.firas.generator.model.config;

/**
 * Spring Boot specific configuration for project generation.
 * 
 * Contains Maven/Gradle and Spring-specific settings that don't apply
 * to other technology stacks.
 * 
 * @author Firas Baklouti
 * @version 1.0
 * @since 2025-12-07
 */
public class SpringConfig {
    
    /** Maven groupId for the generated project (e.g., "com.example") */
    private String groupId = "com.example";
    
    /** Maven artifactId for the generated project (e.g., "demo") */
    private String artifactId = "demo";
    
    /** Java version to use (e.g., "17", "21") */
    private String javaVersion = "17";
    
    /** Spring Boot version to use (e.g., "3.2.0") */
    private String bootVersion = "3.2.0";
    
    /** Build tool: "maven" or "gradle" */
    private String buildTool = "maven";
    
    /** Packaging type: "jar" or "war" */
    private String packaging = "jar";
    
    /** Project structure format: layered, feature, ddd, hexagonal */
    private ProjectStructure projectStructure = ProjectStructure.LAYERED;

    /**
 * Creates a SpringConfig initialized with the class's default configuration values.
 *
 * Defaults: groupId="com.example", artifactId="demo", javaVersion="17", bootVersion="3.2.0",
 * buildTool="maven", packaging="jar", projectStructure=ProjectStructure.LAYERED.
 */
    public SpringConfig() {}
    
    public SpringConfig(String groupId, String artifactId) {
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    // Getters and Setters
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getArtifactId() { return artifactId; }
    public void setArtifactId(String artifactId) { this.artifactId = artifactId; }

    public String getJavaVersion() { return javaVersion; }
    public void setJavaVersion(String javaVersion) { this.javaVersion = javaVersion; }

    public String getBootVersion() { return bootVersion; }
    public void setBootVersion(String bootVersion) { this.bootVersion = bootVersion; }

    public String getBuildTool() { return buildTool; }
    public void setBuildTool(String buildTool) { this.buildTool = buildTool; }

    /**
 * Packaging type used for the generated project.
 *
 * @return the packaging type, either "jar" or "war".
 */
public String getPackaging() { return packaging; }
    /**
 * Set the packaging type for the generated project.
 *
 * @param packaging the packaging type to use (e.g., "jar" or "war")
 */
public void setPackaging(String packaging) { this.packaging = packaging; }

    /**
 * The selected project structure format for the generated project.
 *
 * @return the configured ProjectStructure (for example, LAYERED, FEATURE, DDD, or HEXAGONAL)
 */
public ProjectStructure getProjectStructure() { return projectStructure; }
    /**
 * Set the project structure format used for generated projects.
 *
 * @param projectStructure the project structure to use (e.g., layered, feature, ddd, hexagonal)
 */
public void setProjectStructure(ProjectStructure projectStructure) { this.projectStructure = projectStructure; }
}