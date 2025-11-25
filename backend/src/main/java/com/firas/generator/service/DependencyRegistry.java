package com.firas.generator.service;

import com.firas.generator.model.DependencyGroup;
import com.firas.generator.model.DependencyMetadata;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

@Service
public class DependencyRegistry {
    
    private final Map<String, DependencyMetadata> dependencyMap = new HashMap<>();
    private final List<DependencyGroup> groups = new ArrayList<>();

    @PostConstruct
    public void initialize() {
        // Developer Tools
        DependencyGroup devTools = new DependencyGroup();
        devTools.setName("Developer Tools");
        
        devTools.addDependency(createDependency(
            "devtools", "Spring Boot DevTools",
            "Provides fast application restarts, LiveReload, and configurations for enhanced development experience.",
            "org.springframework.boot", "spring-boot-devtools", null, "runtime"
        ));
        
        devTools.addDependency(createDependency(
            "lombok", "Lombok",
            "Java annotation library which helps to reduce boilerplate code.",
            "org.projectlombok", "lombok", null, "provided"
        ));
        
        devTools.addDependency(createDependency(
            "configuration-processor", "Configuration Processor",
            "Generate metadata for developers to offer contextual help and code completion when working with custom configuration keys.",
            "org.springframework.boot", "spring-boot-configuration-processor", null, "provided"
        ));
        
        groups.add(devTools);
        
        // Web
        DependencyGroup web = new DependencyGroup();
        web.setName("Web");
        
        web.addDependency(createDependency(
            "web", "Spring Web",
            "Build web, including RESTful, applications using Spring MVC. Uses Apache Tomcat as the default embedded container.",
            "org.springframework.boot", "spring-boot-starter-web", null, null
        ));
        
        web.addDependency(createDependency(
            "webflux", "Spring Reactive Web",
            "Build reactive web applications with Spring WebFlux and Netty.",
            "org.springframework.boot", "spring-boot-starter-webflux", null, null
        ));
        
        web.addDependency(createDependency(
            "data-rest", "Rest Repositories",
            "Exposing Spring Data repositories over REST via Spring Data REST.",
            "org.springframework.boot", "spring-boot-starter-data-rest", null, null
        ));
        
        web.addDependency(createDependency(
            "hateoas", "Spring HATEOAS",
            "Eases the creation of RESTful APIs that follow the HATEOAS principle.",
            "org.springframework.boot", "spring-boot-starter-hateoas", null, null
        ));
        
        groups.add(web);
        
        // Security
        DependencyGroup security = new DependencyGroup();
        security.setName("Security");
        
        security.addDependency(createDependency(
            "security", "Spring Security",
            "Highly customizable authentication and access-control framework for Spring applications.",
            "org.springframework.boot", "spring-boot-starter-security", null, null
        ));
        
        security.addDependency(createDependency(
            "oauth2-client", "OAuth2 Client",
            "Spring Boot integration for Spring Security's OAuth2/OpenID Connect client features.",
            "org.springframework.boot", "spring-boot-starter-oauth2-client", null, null
        ));
        
        security.addDependency(createDependency(
            "oauth2-resource-server", "OAuth2 Resource Server",
            "Spring Boot integration for Spring Security's OAuth2 resource server features.",
            "org.springframework.boot", "spring-boot-starter-oauth2-resource-server", null, null
        ));
        
        groups.add(security);
        
        // SQL
        DependencyGroup sql = new DependencyGroup();
        sql.setName("SQL");
        
        sql.addDependency(createDependency(
            "data-jpa", "Spring Data JPA",
            "Persist data in SQL stores with Java Persistence API using Spring Data and Hibernate.",
            "org.springframework.boot", "spring-boot-starter-data-jpa", null, null
        ));
        
        sql.addDependency(createDependency(
            "jdbc", "JDBC API",
            "Database Connectivity API that defines how a client may connect and query a database.",
            "org.springframework.boot", "spring-boot-starter-jdbc", null, null
        ));
        
        sql.addDependency(createDependency(
            "h2", "H2 Database",
            "Provides a fast in-memory database that supports JDBC API and R2DBC access.",
            "com.h2database", "h2", null, "runtime"
        ));
        
        sql.addDependency(createDependency(
            "mysql", "MySQL Driver",
            "MySQL JDBC driver.",
            "com.mysql", "mysql-connector-j", null, "runtime"
        ));
        
        sql.addDependency(createDependency(
            "postgresql", "PostgreSQL Driver",
            "A JDBC and R2DBC driver that allows Java programs to connect to a PostgreSQL database.",
            "org.postgresql", "postgresql", null, "runtime"
        ));
        
        sql.addDependency(createDependency(
            "mariadb", "MariaDB Driver",
            "MariaDB JDBC driver.",
            "org.mariadb.jdbc", "mariadb-java-client", null, "runtime"
        ));
        
        sql.addDependency(createDependency(
            "sqlserver", "MS SQL Server Driver",
            "A JDBC and R2DBC driver that provides access to Microsoft SQL Server and Azure SQL Database.",
            "com.microsoft.sqlserver", "mssql-jdbc", null, "runtime"
        ));
        
        groups.add(sql);
        
        // NoSQL
        DependencyGroup nosql = new DependencyGroup();
        nosql.setName("NoSQL");
        
        nosql.addDependency(createDependency(
            "data-mongodb", "Spring Data MongoDB",
            "Store data in flexible, JSON-like documents, meaning fields can vary from document to document.",
            "org.springframework.boot", "spring-boot-starter-data-mongodb", null, null
        ));
        
        nosql.addDependency(createDependency(
            "data-redis", "Spring Data Redis",
            "Advanced and thread-safe Java Redis client for synchronous, asynchronous, and reactive usage.",
            "org.springframework.boot", "spring-boot-starter-data-redis", null, null
        ));
        
        nosql.addDependency(createDependency(
            "data-elasticsearch", "Spring Data Elasticsearch",
            "A distributed, RESTful search and analytics engine with Spring Data Elasticsearch.",
            "org.springframework.boot", "spring-boot-starter-data-elasticsearch", null, null
        ));
        
        groups.add(nosql);
        
        // I/O
        DependencyGroup io = new DependencyGroup();
        io.setName("I/O");
        
        io.addDependency(createDependency(
            "validation", "Validation",
            "Bean Validation with Hibernate validator.",
            "org.springframework.boot", "spring-boot-starter-validation", null, null
        ));
        
        io.addDependency(createDependency(
            "mail", "Java Mail Sender",
            "Send email using Java Mail and Spring Framework's JavaMailSender.",
            "org.springframework.boot", "spring-boot-starter-mail", null, null
        ));
        
        io.addDependency(createDependency(
            "cache", "Spring Cache",
            "Provides cache-related operations, such as the ability to update the content of the cache.",
            "org.springframework.boot", "spring-boot-starter-cache", null, null
        ));
        
        groups.add(io);
        
        // Ops
        DependencyGroup ops = new DependencyGroup();
        ops.setName("Ops");
        
        ops.addDependency(createDependency(
            "actuator", "Spring Boot Actuator",
            "Supports built-in (or custom) endpoints that let you monitor and manage your application.",
            "org.springframework.boot", "spring-boot-starter-actuator", null, null
        ));
        
        ops.addDependency(createDependency(
            "openapi", "SpringDoc OpenAPI",
            "Automates the generation of API documentation using Spring Boot projects.",
            "org.springdoc", "springdoc-openapi-starter-webmvc-ui", "2.5.0", null
        ));
        
        groups.add(ops);
        
        // Build dependency map
        for (DependencyGroup group : groups) {
            for (DependencyMetadata dep : group.getDependencies()) {
                dependencyMap.put(dep.getId(), dep);
            }
        }
    }
    
    private DependencyMetadata createDependency(String id, String name, String description,
                                                 String groupId, String artifactId, String version, String scope) {
        DependencyMetadata dep = new DependencyMetadata();
        dep.setId(id);
        dep.setName(name);
        dep.setDescription(description);
        dep.setGroupId(groupId);
        dep.setArtifactId(artifactId);
        dep.setVersion(version);
        dep.setScope(scope);
        return dep;
    }

    public List<DependencyGroup> getAllGroups() {
        return groups;
    }

    public DependencyMetadata getDependencyById(String id) {
        return dependencyMap.get(id);
    }

    public List<DependencyMetadata> resolveDependencies(List<String> dependencyIds) {
        List<DependencyMetadata> resolved = new ArrayList<>();
        if (dependencyIds != null) {
            for (String id : dependencyIds) {
                DependencyMetadata dep = getDependencyById(id);
                if (dep != null) {
                    resolved.add(dep);
                }
            }
        }
        return resolved;
    }
}
