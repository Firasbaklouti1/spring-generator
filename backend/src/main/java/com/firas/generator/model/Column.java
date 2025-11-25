package com.firas.generator.model;

public class Column {
    private String name;
    private String type;
    private String fieldName;
    private String javaType;
    private boolean primaryKey;
    private boolean autoIncrement;
    private boolean nullable;
    private boolean foreignKey;
    private String referencedTable;
    private String referencedColumn;
    private boolean unique;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getJavaType() { return javaType; }
    public void setJavaType(String javaType) { this.javaType = javaType; }

    public boolean isPrimaryKey() { return primaryKey; }
    public void setPrimaryKey(boolean primaryKey) { this.primaryKey = primaryKey; }

    public boolean isAutoIncrement() { return autoIncrement; }
    public void setAutoIncrement(boolean autoIncrement) { this.autoIncrement = autoIncrement; }

    public boolean isNullable() { return nullable; }
    public void setNullable(boolean nullable) { this.nullable = nullable; }

    public boolean isForeignKey() { return foreignKey; }
    public void setForeignKey(boolean foreignKey) { this.foreignKey = foreignKey; }

    public String getReferencedTable() { return referencedTable; }
    public void setReferencedTable(String referencedTable) { this.referencedTable = referencedTable; }

    public String getReferencedColumn() { return referencedColumn; }
    public void setReferencedColumn(String referencedColumn) { this.referencedColumn = referencedColumn; }

    public boolean isUnique() { return unique; }
    public void setUnique(boolean unique) { this.unique = unique; }
}
