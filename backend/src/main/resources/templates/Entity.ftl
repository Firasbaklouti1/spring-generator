package ${packageName}.entity;

import jakarta.persistence.*;
<#assign hasRelationships = (table.relationships?? && table.relationships?size > 0)>
<#assign hasCollections = false>
<#if hasRelationships>
<#list table.relationships as rel>
<#if rel.type == "ONE_TO_MANY" || rel.type == "MANY_TO_MANY">
<#assign hasCollections = true>
<#break>
</#if>
</#list>
</#if>
<#if hasCollections>
import java.util.List;
import java.util.ArrayList;
</#if>
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "${table.name}")
public class ${table.className} {

<#list table.columns as column>
    <#if !column.foreignKey>
    <#if column.primaryKey>
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    </#if>
    @Column(name = "${column.name}")
    private ${column.javaType} ${column.fieldName};

    </#if>
</#list>

<#if hasRelationships>
<#list table.relationships as rel>
    <#if rel.type == "MANY_TO_ONE">
    @ManyToOne
    @JoinColumn(name = "${rel.sourceColumn}")
    private ${rel.targetClassName} ${rel.fieldName};

    <#elseif rel.type == "ONE_TO_MANY">
    @OneToMany(mappedBy = "${rel.mappedBy}")
    private List<${rel.targetClassName}> ${rel.fieldName} = new ArrayList<>();

    <#elseif rel.type == "ONE_TO_ONE">
    <#if rel.sourceColumn??>
    @OneToOne
    @JoinColumn(name = "${rel.sourceColumn}")
    private ${rel.targetClassName} ${rel.fieldName};

    <#else>
    @OneToOne(mappedBy = "${rel.mappedBy}")
    private ${rel.targetClassName} ${rel.fieldName};

    </#if>
    <#elseif rel.type == "MANY_TO_MANY">
    @ManyToMany
    @JoinTable(
        name = "${rel.joinTable}",
        joinColumns = @JoinColumn(name = "${rel.sourceColumn}"),
        inverseJoinColumns = @JoinColumn(name = "${rel.targetColumn}")
    )
    private List<${rel.targetClassName}> ${rel.fieldName} = new ArrayList<>();

    </#if>
</#list>
</#if>

<#list table.columns as column>
    <#if !column.foreignKey>
    public ${column.javaType} get${column.fieldName?cap_first}() {
        return ${column.fieldName};
    }

    public void set${column.fieldName?cap_first}(${column.javaType} ${column.fieldName}) {
        this.${column.fieldName} = ${column.fieldName};
    }

    </#if>
</#list>

<#if hasRelationships>
<#list table.relationships as rel>
    <#if rel.type == "ONE_TO_MANY" || rel.type == "MANY_TO_MANY">
    public List<${rel.targetClassName}> get${rel.fieldName?cap_first}() {
        return ${rel.fieldName};
    }

    public void set${rel.fieldName?cap_first}(List<${rel.targetClassName}> ${rel.fieldName}) {
        this.${rel.fieldName} = ${rel.fieldName};
    }

    <#else>
    public ${rel.targetClassName} get${rel.fieldName?cap_first}() {
        return ${rel.fieldName};
    }

    public void set${rel.fieldName?cap_first}(${rel.targetClassName} ${rel.fieldName}) {
        this.${rel.fieldName} = ${rel.fieldName};
    }

    </#if>
</#list>
</#if>
}
