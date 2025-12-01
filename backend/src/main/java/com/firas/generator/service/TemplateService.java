package com.firas.generator.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Service for processing FreeMarker templates to generate code files.
 * 
 * This service uses FreeMarker template engine to generate various code files
 * (Java classes, configuration files, etc.) from templates. It handles template
 * loading, processing, and file writing with proper error handling.
 * 
 * @author Firas Baklouti
 * @version 1.0
 * @since 2025-12-01
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TemplateService {

    /** FreeMarker configuration for template processing */
    private final Configuration freemarkerConfig;

    /**
     * Generates a file from a FreeMarker template.
     * 
     * This method loads the specified template, processes it with the provided data model,
     * and writes the result to the output file. The parent directories of the output file
     * are created automatically if they don't exist.
     * 
     * @param templateName Name of the template file (e.g., \"Entity.ftl\")
     * @param model Data model containing variables to be used in the template
     * @param outputFile The file where the generated content will be written
     * @throws RuntimeException if template processing or file writing fails
     */
    public void generateFile(String templateName, Map<String, Object> model, File outputFile) {
        try (Writer writer = new FileWriter(outputFile)) {
            Template template = freemarkerConfig.getTemplate(templateName);
            template.process(model, writer);
        } catch (IOException | TemplateException e) {
            log.error("Error generating file from template: {}", templateName, e);
            throw new RuntimeException("Failed to generate file", e);
        }
    }
}
