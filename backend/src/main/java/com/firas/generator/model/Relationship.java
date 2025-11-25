package com.firas.generator.model;

public class Relationship {
    private RelationshipType type;
    private String sourceTable;
    private String targetTable;
    private String sourceColumn;
    private String targetColumn;
    private String joinTable;
    private String mappedBy;
    private String fieldName;
    private String targetClassName;

    public Relationship() {
    }

    public Relationship(RelationshipType type, String sourceTable, String targetTable) {
        this.type = type;
        this.sourceTable = sourceTable;
        this.targetTable = targetTable;
    }

    public RelationshipType getType() { return type; }
    public void setType(RelationshipType type) { this.type = type; }

    public String getSourceTable() { return sourceTable; }
    public void setSourceTable(String sourceTable) { this.sourceTable = sourceTable; }

    public String getTargetTable() { return targetTable; }
    public void setTargetTable(String targetTable) { this.targetTable = targetTable; }

    public String getSourceColumn() { return sourceColumn; }
    public void setSourceColumn(String sourceColumn) { this.sourceColumn = sourceColumn; }

    public String getTargetColumn() { return targetColumn; }
    public void setTargetColumn(String targetColumn) { this.targetColumn = targetColumn; }

    public String getJoinTable() { return joinTable; }
    public void setJoinTable(String joinTable) { this.joinTable = joinTable; }

    public String getMappedBy() { return mappedBy; }
    public void setMappedBy(String mappedBy) { this.mappedBy = mappedBy; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getTargetClassName() { return targetClassName; }
    public void setTargetClassName(String targetClassName) { this.targetClassName = targetClassName; }
}
