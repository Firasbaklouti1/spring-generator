package com.firas.generator.model;

import java.util.ArrayList;
import java.util.List;

public class DependencyGroup {
    private String name;
    private List<DependencyMetadata> dependencies = new ArrayList<>();

    public DependencyGroup() {
    }

    public DependencyGroup(String name) {
        this.name = name;
    }

    public void addDependency(DependencyMetadata dependency) {
        this.dependencies.add(dependency);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<DependencyMetadata> getDependencies() { return dependencies; }
    public void setDependencies(List<DependencyMetadata> dependencies) { this.dependencies = dependencies; }
}
