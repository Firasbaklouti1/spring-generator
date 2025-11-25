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

@Service
@Slf4j
@RequiredArgsConstructor
public class TemplateService {

    private final Configuration freemarkerConfig;

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
