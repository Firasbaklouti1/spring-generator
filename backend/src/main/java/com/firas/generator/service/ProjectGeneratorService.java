package com.firas.generator.service;

import com.firas.generator.model.ProjectRequest;
import java.io.File;
import java.io.IOException;

public interface ProjectGeneratorService {
    byte[] generateProject(ProjectRequest request) throws IOException;
}
