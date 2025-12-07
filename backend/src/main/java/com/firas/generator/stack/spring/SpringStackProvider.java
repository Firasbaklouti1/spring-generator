package com.firas.generator.stack.spring;

import com.firas.generator.model.DependencyMetadata;
import com.firas.generator.model.FilePreview;
import com.firas.generator.model.ProjectRequest;
import com.firas.generator.model.Table;
import com.firas.generator.model.config.SpringConfig;
import com.firas.generator.service.TemplateService;
import com.firas.generator.stack.*;
import com.firas.generator.util.ZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Stack provider for Spring Boot projects.
 * 
 * Generates complete Spring Boot projects with Maven, JPA entities,
 * Spring Data repositories, services, and REST controllers.
 * 
 * This provider encapsulates all Spring-specific generation logic,
 * delegating to SpringCodeGenerator for CRUD code and SpringTypeMapper
 * for type conversions.
 * 
 * @author Firas Baklouti
 * @version 1.0
 * @since 2025-12-07
 */
@Slf4j
@Component
public class SpringStackProvider implements StackProvider {
    
    private static final String TEMPLATE_DIR = "spring/";
    
    private final TemplateService templateService;
    private final SpringCodeGenerator codeGenerator;
    private final SpringTypeMapper typeMapper;
    private final SpringDependencyProvider dependencyProvider;
    
    public SpringStackProvider(
            TemplateService templateService,
            SpringCodeGenerator codeGenerator,
            SpringTypeMapper typeMapper,
            SpringDependencyProvider dependencyProvider) {
        this.templateService = templateService;
        this.codeGenerator = codeGenerator;
        this.typeMapper = typeMapper;
        this.dependencyProvider = dependencyProvider;
    }
    
    @Override
    public StackType getStackType() {
        return StackType.SPRING;
    }
    
    @Override
    public String getTemplateDirectory() {
        return "spring";
    }
    
    @Override
    public TypeMapper getTypeMapper() {
        return typeMapper;
    }
    
    @Override
    public CodeGenerator getCodeGenerator() {
        return codeGenerator;
    }
    
    @Override
    public DependencyProvider getDependencyProvider() {
        return dependencyProvider;
    }
    
    @Override
    public List<FilePreview> generateProject(ProjectRequest request) throws IOException {
        // Apply type mappings to all columns
        applyTypeMappings(request);
        
        List<FilePreview> files = new ArrayList<>();
        
        // Generate project structure files
        files.add(generatePom(request));
        files.add(generateMainClass(request));
        files.add(generateApplicationProperties(request));
        
        // Generate CRUD code if tables are provided
        if (request.getTables() != null && !request.getTables().isEmpty()) {
            for (Table table : request.getTables()) {
                if (table.isJoinTable()) {
                    continue; // Skip join tables
                }
                
                if (request.isIncludeEntity()) {
                    files.add(codeGenerator.generateEntity(table, request.getPackageName()));
                }
                if (request.isIncludeRepository()) {
                    files.add(codeGenerator.generateRepository(table, request.getPackageName()));
                }
                if (request.isIncludeService()) {
                    files.add(codeGenerator.generateService(table, request.getPackageName()));
                }
                if (request.isIncludeController()) {
                    files.add(codeGenerator.generateController(table, request.getPackageName()));
                }
                if (request.isIncludeDto()) {
                    files.add(codeGenerator.generateDto(table, request.getPackageName()));
                }
                if (request.isIncludeMapper()) {
                    files.add(codeGenerator.generateMapper(table, request.getPackageName()));
                }
            }
        }
        
        return files;
    }
    
    @Override
    public byte[] generateProjectZip(ProjectRequest request) throws IOException {
        List<FilePreview> files = generateProject(request);
        return createZipFromFiles(files, getProjectName(request));
    }
    
    // ==================== Spring-Specific Generation Methods ====================
    
    /**
     * Generates the Maven pom.xml file.
     */
    private FilePreview generatePom(ProjectRequest request) {
        SpringConfig config = request.getEffectiveSpringConfig();
        
        Map<String, Object> model = new HashMap<>();
        model.put("request", request);
        model.put("springConfig", config);
        
        List<DependencyMetadata> dependencies = request.getDependencies();
        if (dependencies == null) {
            dependencies = new ArrayList<>();
        }
        model.put("dependencies", dependencies);
        
        // Check if Lombok is in dependencies
        boolean hasLombok = dependencies.stream()
                .anyMatch(dep -> "lombok".equals(dep.getId()));
        model.put("hasLombok", hasLombok);
        
        String content = templateService.processTemplateToString(TEMPLATE_DIR + "pom.xml.ftl", model);
        return new FilePreview("pom.xml", content, "xml");
    }
    
    /**
     * Generates the main Spring Boot application class.
     */
    private FilePreview generateMainClass(ProjectRequest request) {
        Map<String, Object> model = new HashMap<>();
        model.put("request", request);
        
        String className = toClassName(request.getName()) + "Application";
        model.put("className", className);
        
        String content = templateService.processTemplateToString(TEMPLATE_DIR + "Application.java.ftl", model);
        String packagePath = request.getPackageName().replace(".", "/");
        String path = "src/main/java/" + packagePath + "/" + className + ".java";
        
        return new FilePreview(path, content, "java");
    }
    
    /**
     * Generates the application.properties file.
     */
    private FilePreview generateApplicationProperties(ProjectRequest request) {
        Map<String, Object> model = new HashMap<>();
        model.put("request", request);
        
        String content = templateService.processTemplateToString(TEMPLATE_DIR + "application.properties.ftl", model);
        return new FilePreview("src/main/resources/application.properties", content, "properties");
    }
    
    // ==================== Utility Methods ====================
    
    /**
     * Converts a project name to a valid Java class name.
     */
    private String toClassName(String name) {
        if (name == null || name.isEmpty()) return "Demo";
        // Remove non-alphanumeric characters and capitalize
        String cleaned = name.replaceAll("[^a-zA-Z0-9]", "");
        if (cleaned.isEmpty()) return "Demo";
        return cleaned.substring(0, 1).toUpperCase() + cleaned.substring(1);
    }
    
    /**
     * Gets the project name from the request.
     * Uses artifactId from SpringConfig or falls back to project name.
     */
    private String getProjectName(ProjectRequest request) {
        SpringConfig config = request.getEffectiveSpringConfig();
        
        if (config.getArtifactId() != null && !config.getArtifactId().isEmpty()) {
            return config.getArtifactId();
        }
        if (request.getName() != null && !request.getName().isEmpty()) {
            return request.getName().toLowerCase().replace(" ", "-");
        }
        return "spring-project";
    }
    
    /**
     * Creates a ZIP file from the list of file previews.
     */
    private byte[] createZipFromFiles(List<FilePreview> files, String projectName) throws IOException {
        Path tempDir = Files.createTempDirectory("spring-gen-");
        File projectDir = new File(tempDir.toFile(), projectName);
        projectDir.mkdirs();
        
        try {
            // Write all files to temp directory
            for (FilePreview file : files) {
                Path filePath = projectDir.toPath().resolve(file.getPath());
                Files.createDirectories(filePath.getParent());
                Files.writeString(filePath, file.getContent(), StandardCharsets.UTF_8);
            }
            
            // Create ZIP
            return ZipUtils.zipDirectory(projectDir);
        } finally {
            // Cleanup temp directory
            try {
                FileUtils.deleteDirectory(tempDir.toFile());
            } catch (IOException e) {
                log.warn("Failed to clean up temp directory: {}", tempDir, e);
            }
        }
    }
}
