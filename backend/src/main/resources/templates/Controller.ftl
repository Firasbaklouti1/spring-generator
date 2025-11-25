package ${packageName}.controller;

import ${packageName}.entity.${table.className};
import ${packageName}.service.${table.className}Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/${table.className?lower_case}s")
public class ${table.className}Controller {

    private final ${table.className}Service service;

    public ${table.className}Controller(${table.className}Service service) {
    this.service = service;
    }

    @GetMapping
    public List<${table.className}> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<${table.className}> getById(@PathVariable <#list table.columns as col><#if col.primaryKey>${col.javaType}</#if></#list> id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ${table.className} create(@RequestBody ${table.className} entity) {
        return service.save(entity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable <#list table.columns as col><#if col.primaryKey>${col.javaType}</#if></#list> id) {
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
