package com.firas.generator.controller;

import com.firas.generator.model.ProjectRequest;
import com.firas.generator.service.ProjectGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/generate")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow all for now
public class GeneratorController {

    private final ProjectGeneratorService projectGeneratorService;

    @PostMapping("/project")
    public ResponseEntity<byte[]> generateProject(@RequestBody ProjectRequest request) throws IOException {
        byte[] zipContent = projectGeneratorService.generateProject(request);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + request.getArtifactId() + ".zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zipContent);
    }
}
