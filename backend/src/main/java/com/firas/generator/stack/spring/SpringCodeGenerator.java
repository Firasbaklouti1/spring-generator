package com.firas.generator.stack.spring;

import com.firas.generator.model.FilePreview;
import com.firas.generator.model.Table;
import com.firas.generator.model.config.ProjectStructure;
import com.firas.generator.model.config.SecurityConfig;
import com.firas.generator.model.config.SecurityRule;
import com.firas.generator.model.config.SpringConfig;
import com.firas.generator.service.TemplateService;
import com.firas.generator.stack.CodeGenerator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Code generator for Spring Boot projects.
 * 
 * Generates JPA entities, Spring Data repositories, services, and REST controllers
 * from table metadata using FreeMarker templates.
 * 
 * Supports multiple project structures:
 * - LAYERED: Traditional folder-by-type (entity/, repository/, service/, controller/)
 * - FEATURE: Folder-by-feature (user/User.java, user/UserRepository.java, etc.)
 * - DDD: Domain-Driven Design (domain/user/entity/, domain/user/repository/)
 * - HEXAGONAL: Hexagonal/Clean Architecture (domain/model/, infrastructure/adapter/)
 * 
 * @author Firas Baklouti
 * @version 1.1
 * @since 2025-12-07
 */
@Component
public class SpringCodeGenerator implements CodeGenerator {
    
    private static final String TEMPLATE_DIR = "spring/";
    
    private final TemplateService templateService;
    
    // Security configuration for controller generation
    private SecurityConfig securityConfig;
    
    // Spring configuration for project structure
    private SpringConfig springConfig;
    
    /**
     * Create a SpringCodeGenerator configured with the provided template rendering service.
     *
     * @param templateService the TemplateService used to render FreeMarker templates into generated file content
     */
    public SpringCodeGenerator(TemplateService templateService) {
        this.templateService = templateService;
    }
    
    /**
     * Sets the security configuration used by the generator to include security-related behavior in generated controllers.
     *
     * @param securityConfig the security configuration to apply, or `null` to disable security features
     */
    public void setSecurityConfig(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }
    
    /**
     * Sets the Spring configuration used to determine project structure and related settings for code generation.
     *
     * @param springConfig the SpringConfig instance to use; may be null to fall back to default settings (LAYERED)
     */
    public void setSpringConfig(SpringConfig springConfig) {
        this.springConfig = springConfig;
    }
    
    /**
     * Determine the active project structure from the configured SpringConfig, defaulting to ProjectStructure.LAYERED when not configured.
     *
     * @return the active ProjectStructure; ProjectStructure.LAYERED if springConfig or its project structure is null
     */
    private ProjectStructure getProjectStructure() {
        if (springConfig != null && springConfig.getProjectStructure() != null) {
            return springConfig.getProjectStructure();
        }
        return ProjectStructure.LAYERED;
    }
    
    /**
     * Compute the file system path for a generated Java source file according to the active project structure.
     *
     * @param packageName base Java package (dot-separated)
     * @param table       table metadata used to derive the class and feature names
     * @param fileType    logical file type such as "entity", "repository", "service", "controller", "dto", or "mapper"
     * @param suffix      filename suffix appended to the entity class name (may be empty), e.g. "Repository", "Service"
     * @param isTest      true to place the file under src/test/java, false to place under src/main/java
     * @return            the relative file path for the generated Java file (e.g. src/main/java/com/example/entity/UserRepository.java)
     */
    private String generatePath(String packageName, Table table, String fileType, String suffix, boolean isTest) {
        String baseDir = isTest ? "src/test/java/" : "src/main/java/";
        String packagePath = packageName.replace(".", "/");
        String className = table.getClassName();
        String featureName = className.toLowerCase();
        String fileName = className + suffix + ".java";
        
        ProjectStructure structure = getProjectStructure();
        
        return switch (structure) {
            case LAYERED -> 
                // Traditional: entity/, repository/, service/, controller/
                baseDir + packagePath + "/" + fileType + "/" + fileName;
            
            case FEATURE -> 
                // Feature-based: user/User.java, user/UserRepository.java
                baseDir + packagePath + "/" + featureName + "/" + fileName;
            
            case DDD -> {
                // DDD: domain/user/entity/, domain/user/repository/, etc.
                // Controllers go in application/controller/
                if ("controller".equals(fileType)) {
                    yield baseDir + packagePath + "/application/controller/" + fileName;
                }
                yield baseDir + packagePath + "/domain/" + featureName + "/" + fileType + "/" + fileName;
            }
            
            case HEXAGONAL -> {
                // Hexagonal: domain/model/, infrastructure/adapter/
                yield switch (fileType) {
                    case "entity" -> 
                        baseDir + packagePath + "/domain/model/" + fileName;
                    case "repository" -> 
                        baseDir + packagePath + "/infrastructure/adapter/out/persistence/" + fileName;
                    case "service" -> 
                        baseDir + packagePath + "/application/service/" + fileName;
                    case "controller" -> 
                        baseDir + packagePath + "/infrastructure/adapter/in/web/" + fileName;
                    case "dto" -> 
                        baseDir + packagePath + "/application/dto/" + fileName;
                    case "mapper" -> 
                        baseDir + packagePath + "/application/mapper/" + fileName;
                    default -> 
                        baseDir + packagePath + "/" + fileType + "/" + fileName;
                };
            }
        };
    }
    
    /**
     * Gets the effective package name for the file based on structure.
     * This is used in templates to set the correct package declaration.
     */
    private String getEffectivePackage(String basePackage, Table table, String fileType) {
        String featureName = table.getClassName().toLowerCase();
        ProjectStructure structure = getProjectStructure();
        
        return switch (structure) {
            case LAYERED -> basePackage + "." + fileType;
            case FEATURE -> basePackage + "." + featureName;
            case DDD -> {
                if ("controller".equals(fileType)) {
                    yield basePackage + ".application.controller";
                }
                yield basePackage + ".domain." + featureName + "." + fileType;
            }
            case HEXAGONAL -> switch (fileType) {
                case "entity" -> basePackage + ".domain.model";
                case "repository" -> basePackage + ".infrastructure.adapter.out.persistence";
                case "service" -> basePackage + ".application.service";
                case "controller" -> basePackage + ".infrastructure.adapter.in.web";
                case "dto" -> basePackage + ".application.dto";
                case "mapper" -> basePackage + ".application.mapper";
                default -> basePackage + "." + fileType;
            };
        };
    }
    
    /**
     * Generate a FilePreview for an entity Java source file using the table metadata and active project structure.
     *
     * @param table       metadata describing the database table used to populate the entity template
     * @param packageName the base package name to use when resolving the effective package and file path
     * @return            a FilePreview containing the generated file path and Java source content for the entity
     */
    @Override
    public FilePreview generateEntity(Table table, String packageName) {
        String effectivePackage = getEffectivePackage(packageName, table, "entity");
        Map<String, Object> model = createModel(table, packageName, effectivePackage, "entity");
        
        String content = templateService.processTemplateToString(TEMPLATE_DIR + "Entity.ftl", model);
        String path = generatePath(packageName, table, "entity", "", false);
        
        return new FilePreview(path, content, "java");
    }
    
    /**
     * Generate a Spring Data repository file preview for the given table.
     *
     * @param table       metadata for the database table to base the repository on
     * @param packageName the project's base Java package used to compute the file package and path
     * @return            a FilePreview containing the target file path, the generated Java source for the repository, and the "java" file extension
     */
    @Override
    public FilePreview generateRepository(Table table, String packageName) {
        String effectivePackage = getEffectivePackage(packageName, table, "repository");
        Map<String, Object> model = createModel(table, packageName, effectivePackage, "repository");
        
        String content = templateService.processTemplateToString(TEMPLATE_DIR + "Repository.ftl", model);
        String path = generatePath(packageName, table, "repository", "Repository", false);
        
        return new FilePreview(path, content, "java");
    }
    
    /**
     * Generate the service class file preview for the given table.
     *
     * @param table       metadata of the table used to build the service class
     * @param packageName base package name used to compute the effective package and file path
     * @return            a FilePreview containing the generated service class path, source content, and file type
     */
    @Override
    public FilePreview generateService(Table table, String packageName) {
        String effectivePackage = getEffectivePackage(packageName, table, "service");
        Map<String, Object> model = createModel(table, packageName, effectivePackage, "service");
        
        String content = templateService.processTemplateToString(TEMPLATE_DIR + "Service.ftl", model);
        String path = generatePath(packageName, table, "service", "Service", false);
        
        return new FilePreview(path, content, "java");
    }
    
    /**
     * Generates a controller file preview for the given database table and base package, including security rules when configured.
     *
     * @param table metadata describing the entity for which to generate the controller
     * @param packageName base package used to compute the controller's package and file path
     * @return a FilePreview containing the generated controller's file path and Java source content
     */
    @Override
    public FilePreview generateController(Table table, String packageName) {
        String effectivePackage = getEffectivePackage(packageName, table, "controller");
        Map<String, Object> model = createModel(table, packageName, effectivePackage, "controller");
        
        // Add security configuration to controller model
        if (securityConfig != null && securityConfig.isEnabled()) {
            model.put("securityEnabled", true);
            
            // Filter security rules for this entity's endpoints
            if (securityConfig.getRules() != null) {
                String basePath = "/api/" + table.getClassName().toLowerCase();
                List<SecurityRule> entityRules = securityConfig.getRules().stream()
                    .filter(rule -> rule.getPath() != null && 
                            (rule.getPath().startsWith(basePath + "/") || 
                             rule.getPath().equals(basePath + "/**")))
                    .collect(Collectors.toList());
                model.put("securityRules", entityRules);
            }
        } else {
            model.put("securityEnabled", false);
        }
        
        String content = templateService.processTemplateToString(TEMPLATE_DIR + "Controller.ftl", model);
        String path = generatePath(packageName, table, "controller", "Controller", false);
        
        return new FilePreview(path, content, "java");
    }
    
    /**
     * Generates a DTO source file preview for the given table within the specified base package.
     *
     * @param table metadata describing the database table and target class name
     * @param packageName the project's base Java package used to determine output package and path
     * @return a FilePreview containing the file path and Java content placeholder for the DTO
     */
    @Override
    public FilePreview generateDto(Table table, String packageName) {
        String effectivePackage = getEffectivePackage(packageName, table, "dto");
        Map<String, Object> model = createModel(table, packageName, effectivePackage, "dto");
        
        // TODO: Add Dto.ftl template for Spring
        String content = "// DTO for " + table.getClassName() + "\n// TODO: Implement DTO template";
        String path = generatePath(packageName, table, "dto", "Dto", false);
        
        return new FilePreview(path, content, "java");
    }
    
    /**
     * Generate a mapper class preview for the given table and base package.
     *
     * The returned preview contains the file path where the mapper should be created
     * (according to the active project structure), Java source content (currently a placeholder comment until a template is provided),
     * and the "java" file type.
     *
     * @param table       metadata describing the database table and target entity (used to derive class names and feature)
     * @param packageName the base Java package to use when computing the effective package and file path
     * @return            a FilePreview with the target path, placeholder mapper source content, and "java" as the file type
     */
    @Override
    public FilePreview generateMapper(Table table, String packageName) {
        String effectivePackage = getEffectivePackage(packageName, table, "mapper");
        Map<String, Object> model = createModel(table, packageName, effectivePackage, "mapper");
        
        // TODO: Add Mapper.ftl template for Spring
        String content = "// Mapper for " + table.getClassName() + "\n// TODO: Implement Mapper template";
        String path = generatePath(packageName, table, "mapper", "Mapper", false);
        
        return new FilePreview(path, content, "java");
    }
    
    /**
     * Generates a JUnit test for the repository layer.
     *
     * @param table the table metadata used to generate the test
     * @param packageName the base package name to use when computing the test's package and path
     * @return a FilePreview containing the generated test file path, its source content, and the language ("java")
     */
    public FilePreview generateRepositoryTest(Table table, String packageName) {
        String effectivePackage = getEffectivePackage(packageName, table, "repository");
        Map<String, Object> model = createModel(table, packageName, effectivePackage, "repository");
        
        String content = templateService.processTemplateToString(TEMPLATE_DIR + "RepositoryTest.ftl", model);
        String path = generatePath(packageName, table, "repository", "RepositoryTest", true);
        
        return new FilePreview(path, content, "java");
    }
    
    /**
     * Generates a JUnit MockMvc test class for the controller corresponding to the given table.
     *
     * @param table metadata of the database table whose controller is being tested
     * @param packageName base Java package used to compute the file's package and path
     * @return a FilePreview containing the generated test file's path, content, and "java" extension
     */
    public FilePreview generateControllerTest(Table table, String packageName) {
        String effectivePackage = getEffectivePackage(packageName, table, "controller");
        Map<String, Object> model = createModel(table, packageName, effectivePackage, "controller");
        
        String content = templateService.processTemplateToString(TEMPLATE_DIR + "ControllerTest.ftl", model);
        String path = generatePath(packageName, table, "controller", "ControllerTest", true);
        
        return new FilePreview(path, content, "java");
    }
    
    /**
     * Build the template model for a table, including structure-aware package and project-structure id.
     *
     * If the table exposes metadata, those entries are merged into the returned model.
     *
     * @param table the table metadata used to populate the model
     * @param basePackageName the project's base package name
     * @param effectivePackage the package name to use for the generated file, adjusted for project structure
     * @param fileType the kind of file being generated (e.g., "entity", "controller"); included for context in the model
     * @return a map of model values for template rendering (keys include "table", "packageName", "basePackageName", "projectStructure" and any table metadata)
     */
    private Map<String, Object> createModel(Table table, String basePackageName, String effectivePackage, String fileType) {
        Map<String, Object> model = new HashMap<>();
        model.put("table", table);
        model.put("packageName", effectivePackage);
        model.put("basePackageName", basePackageName);
        model.put("projectStructure", getProjectStructure().getId());

        if (table.getMetadata() != null) {
            System.out.println("DEBUG: Table " + table.getName() + " has metadata: " + table.getMetadata());
            model.putAll(table.getMetadata());
        } else {
            System.out.println("DEBUG: Table " + table.getName() + " has NULL metadata");
        }
        return model;
    }
}