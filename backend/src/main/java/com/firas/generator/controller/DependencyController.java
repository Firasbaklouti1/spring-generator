package com.firas.generator.controller;

import com.firas.generator.model.DependencyGroup;
import com.firas.generator.service.DependencyRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dependencies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DependencyController {

    private final DependencyRegistry dependencyRegistry;

    @GetMapping("/groups")
    public List<DependencyGroup> getDependencyGroups() {
        return dependencyRegistry.getAllGroups();
    }
}
