package com.firas.generator.service.impl;

import com.firas.generator.model.DependencyMetadata;
import com.firas.generator.model.ProjectRequest;
import com.firas.generator.model.Table;
import com.firas.generator.service.DependencyRegistry;
import com.firas.generator.service.ProjectGeneratorService;
import com.firas.generator.service.TemplateService;
import com.firas.generator.util.SqlParser;
import com.firas.generator.util.ZipUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of ProjectGeneratorService for generating Spring Boot projects.
 * 
 * This service orchestrates the entire project generation process:
 * 1. Creates the standard Maven project structure (src/main/java, src/main/resources, etc.)
 * 2. Generates pom.xml with selected dependencies
 * 3. Generates the main Application class
 * 4. Generates application.properties configuration file
 * 5. Optionally generates CRUD code (entities, repositories, services, controllers) from SQL schemas
 * 6. Packages everything into a ZIP file for download
 * 
 * The service uses FreeMarker templates for code generation and creates projects in temporary
 * directories that are cleaned up after ZIP creation.
 * 
 * @author Firas Baklouti
 * @version 1.0
 * @since 2025-12-01
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectGeneratorServiceImpl implements ProjectGeneratorService {

    /** Service for processing FreeMarker templates */
    private final TemplateService templateService;
    
    /** Utility for parsing SQL schemas */
    private final SqlParser sqlParser;
    
    /** Registry for resolving dependency metadata */
    private final DependencyRegistry dependencyRegistry;

    /**
     * Generates a complete Spring Boot project based on the provided request.
     * 
     * This method creates a temporary directory, generates all project files,
     * packages them into a ZIP file, and cleans up the temporary directory.
     * 
     * @param request The project configuration
     * @return Byte array containing the ZIP file
     * @throws IOException If an error occurs during generation
     */
    @Override
    public byte[] generateProject(ProjectRequest request) throws IOException {
        Path tempDir = Files.createTempDirectory("project-gen-");
        File rootDir = tempDir.toFile();
        
        try {
            String baseDirName = request.getArtifactId();
            File projectDir = new File(rootDir, baseDirName);
            projectDir.mkdirs();

            // Generate structure
            generateStructure(projectDir, request);
            
            // Generate pom.xml
            generatePom(projectDir, request);
            
            // Generate Main Class
            generateMainClass(projectDir, request);
            
            // Generate Application Properties
            generateApplicationProperties(projectDir, request);
            
            // Generate CRUD from SQL
            if (request.getSqlContent() != null && !request.getSqlContent().isEmpty()) {
                generateCrud(projectDir, request);
            }

            // Zip the directory
            return ZipUtils.zipDirectory(projectDir);
            
        } finally {
            FileUtils.deleteDirectory(rootDir);
        }
    }

    /**
     * Creates the standard Maven project directory structure.
     * 
     * @param projectDir The root project directory
     * @param request The project configuration
     */
    private void generateStructure(File projectDir, ProjectRequest request) {
        String packagePath = request.getPackageName().replace(".", "/");
        File javaSrc = new File(projectDir, "src/main/java/" + packagePath);
        javaSrc.mkdirs();
        
        File resources = new File(projectDir, "src/main/resources");
        resources.mkdirs();
        
        File testSrc = new File(projectDir, "src/test/java/" + packagePath);
        testSrc.mkdirs();
    }

    /**
     * Generates the Maven pom.xml file with dependencies.
     * 
     * @param projectDir The root project directory
     * @param request The project configuration
     */
    private void generatePom(File projectDir, ProjectRequest request) {
        Map<String, Object> model = new HashMap<>();
        model.put("request", request);
        
        // Resolve dependencies from registry
        List<DependencyMetadata> resolvedDependencies = dependencyRegistry.resolveDependencies(request.getDependencies());
        model.put("dependencies", resolvedDependencies);
        
        // Check if Lombok is in dependencies
        boolean hasLombok = resolvedDependencies.stream()
                .anyMatch(dep -> "lombok".equals(dep.getId()));
        model.put("hasLombok", hasLombok);
        
        templateService.generateFile("pom.xml.ftl", model, new File(projectDir, "pom.xml"));
    }

    /**
     * Generates the main Spring Boot application class.
     * 
     * @param projectDir The root project directory
     * @param request The project configuration
     */
    private void generateMainClass(File projectDir, ProjectRequest request) {
        String packagePath = request.getPackageName().replace(".", "/");
        File javaSrc = new File(projectDir, "src/main/java/" + packagePath);
        
        Map<String, Object> model = new HashMap<>();
        model.put("request", request);
        
        String className = toClassName(request.getName()) + "Application";
        model.put("className", className);
        
        templateService.generateFile("Application.java.ftl", model, new File(javaSrc, className + ".java"));
    }
    
    /**
     * Generates the application.properties configuration file.
     * 
     * @param projectDir The root project directory
     * @param request The project configuration
     */
    private void generateApplicationProperties(File projectDir, ProjectRequest request) {
        File resources = new File(projectDir, "src/main/resources");
        Map<String, Object> model = new HashMap<>();
        model.put("request", request);
        templateService.generateFile("application.properties.ftl", model, new File(resources, "application.properties"));
    }

    /**
     * Generates CRUD code (entities, repositories, services, controllers) from SQL schema.
     * 
     * Parses the SQL content from the request and generates JPA entities with their
     * corresponding repository, service, and controller classes.
     * 
     * @param projectDir The root project directory
     * @param request The project configuration containing SQL content
     */
    private void generateCrud(File projectDir, ProjectRequest request) {
        List<Table> tables = sqlParser.parseSql(request.getSqlContent());
        String packagePath = request.getPackageName().replace(".", "/");
        File javaSrc = new File(projectDir, "src/main/java/" + packagePath);
        
        for (Table table : tables) {
            Map<String, Object> model = new HashMap<>();
            model.put("table", table);
            model.put("packageName", request.getPackageName());
            
            // Entity
            createFile(javaSrc, "entity", table.getClassName() + ".java", "Entity.ftl", model);
            
            // Repository
            createFile(javaSrc, "repository", table.getClassName() + "Repository.java", "Repository.ftl", model);
            
            // Service
            createFile(javaSrc, "service", table.getClassName() + "Service.java", "Service.ftl", model);
            
            // Controller
            createFile(javaSrc, "controller", table.getClassName() + "Controller.java", "Controller.ftl", model);
        }
    }
    
    /**
     * Creates a file from a template in a specific package subdirectory.
     * 
     * @param javaSrc The base Java source directory
     * @param subPackage The sub-package name (e.g., "entity", "repository")
     * @param fileName The output file name
     * @param templateName The FreeMarker template name
     * @param model The data model for template processing
     */
    private void createFile(File javaSrc, String subPackage, String fileName, String templateName, Map<String, Object> model) {
        File packageDir = new File(javaSrc, subPackage);
        packageDir.mkdirs();
        templateService.generateFile(templateName, model, new File(packageDir, fileName));
    }

    /**
     * Converts a project name to a valid Java class name.
     * 
     * @param name The project name
     * @return Capitalized class name (defaults to "Demo" if name is empty)
     */
    private String toClassName(String name) {
        if (name == null || name.isEmpty()) return "Demo";
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
