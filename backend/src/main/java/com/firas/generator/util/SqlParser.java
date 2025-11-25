package com.firas.generator.util;

import com.firas.generator.model.Column;
import com.firas.generator.model.Relationship;
import com.firas.generator.model.RelationshipType;
import com.firas.generator.model.Table;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SqlParser {

    private static final Pattern TABLE_PATTERN = Pattern.compile("CREATE\\s+TABLE\\s+`?(\\w+)`?\\s*\\((.*?)\\);", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern COLUMN_PATTERN = Pattern.compile("`?(\\w+)`?\\s+(\\w+)(\\(.*?\\))?\\s*(.*)");
    private static final Pattern FK_CONSTRAINT_PATTERN = Pattern.compile("FOREIGN\\s+KEY\\s*\\(`?(\\w+)`?\\)\\s*REFERENCES\\s+`?(\\w+)`?\\s*\\(`?(\\w+)`?\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern INLINE_FK_PATTERN = Pattern.compile("REFERENCES\\s+`?(\\w+)`?\\s*\\(`?(\\w+)`?\\)", Pattern.CASE_INSENSITIVE);

    public List<Table> parseSql(String sql) {
        List<Table> tables = new ArrayList<>();
        Map<String, Table> tableMap = new HashMap<>();
        Matcher tableMatcher = TABLE_PATTERN.matcher(sql);

        // First pass: parse tables and columns
        while (tableMatcher.find()) {
            Table table = new Table();
            String tableName = tableMatcher.group(1);
            table.setName(tableName);
            table.setClassName(toClassName(tableName));
            
            String columnsBlock = tableMatcher.group(2);
            String[] lines = columnsBlock.split(",");
            
            for (String line : lines) {
                line = line.trim();
                
                // Check for FOREIGN KEY constraint
                Matcher fkMatcher = FK_CONSTRAINT_PATTERN.matcher(line);
                if (fkMatcher.find()) {
                    String columnName = fkMatcher.group(1);
                    String refTable = fkMatcher.group(2);
                    String refColumn = fkMatcher.group(3);
                    
                    // Find the column and mark it as FK
                    for (Column col : table.getColumns()) {
                        if (col.getName().equals(columnName)) {
                            col.setForeignKey(true);
                            col.setReferencedTable(refTable);
                            col.setReferencedColumn(refColumn);
                            break;
                        }
                    }
                    continue;
                }
                
                if (line.isEmpty() || line.toUpperCase().startsWith("PRIMARY KEY") || 
                    line.toUpperCase().startsWith("CONSTRAINT") || line.toUpperCase().startsWith("UNIQUE KEY")) {
                    continue;
                }
                
                Matcher colMatcher = COLUMN_PATTERN.matcher(line);
                if (colMatcher.find()) {
                    Column column = new Column();
                    String colName = colMatcher.group(1);
                    String colType = colMatcher.group(2);
                    String extras = colMatcher.group(4);
                    
                    column.setName(colName);
                    column.setType(colType);
                    column.setFieldName(toFieldName(colName));
                    column.setJavaType(mapToJavaType(colType));
                    
                    if (extras != null) {
                        if (extras.toUpperCase().contains("PRIMARY KEY")) {
                            column.setPrimaryKey(true);
                        }
                        if (extras.toUpperCase().contains("AUTO_INCREMENT")) {
                            column.setAutoIncrement(true);
                        }
                        if (extras.toUpperCase().contains("UNIQUE")) {
                            column.setUnique(true);
                        }
                        
                        // Check for inline REFERENCES
                        Matcher inlineFkMatcher = INLINE_FK_PATTERN.matcher(extras);
                        if (inlineFkMatcher.find()) {
                            column.setForeignKey(true);
                            column.setReferencedTable(inlineFkMatcher.group(1));
                            column.setReferencedColumn(inlineFkMatcher.group(2));
                        }
                    }
                    
                    table.addColumn(column);
                }
            }
            tables.add(table);
            tableMap.put(tableName, table);
        }
        
        // Second pass: build relationships
        buildRelationships(tables, tableMap);
        
        // Filter out join tables from the final list
        tables.removeIf(Table::isJoinTable);
        
        return tables;
    }

    private void buildRelationships(List<Table> tables, Map<String, Table> tableMap) {
        for (Table table : tables) {
            // Check if this is a join table (for many-to-many)
            List<Column> foreignKeys = new ArrayList<>();
            for (Column col : table.getColumns()) {
                if (col.isForeignKey()) {
                    foreignKeys.add(col);
                }
            }
            
            // A join table typically has 2 foreign keys and no other significant columns
            if (foreignKeys.size() == 2 && table.getColumns().size() <= 3) {
                table.setJoinTable(true);
                
                // Create many-to-many relationships
                Column fk1 = foreignKeys.get(0);
                Column fk2 = foreignKeys.get(1);
                
                Table table1 = tableMap.get(fk1.getReferencedTable());
                Table table2 = tableMap.get(fk2.getReferencedTable());
                
                if (table1 != null && table2 != null) {
                    // Add many-to-many to table1
                    Relationship rel1 = new Relationship(RelationshipType.MANY_TO_MANY, table1.getName(), table2.getName());
                    rel1.setJoinTable(table.getName());
                    rel1.setSourceColumn(fk1.getName());
                    rel1.setTargetColumn(fk2.getName());
                    rel1.setFieldName(pluralize(toFieldName(table2.getName())));
                    rel1.setTargetClassName(table2.getClassName());
                    table1.addRelationship(rel1);
                    
                    // Add many-to-many to table2
                    Relationship rel2 = new Relationship(RelationshipType.MANY_TO_MANY, table2.getName(), table1.getName());
                    rel2.setJoinTable(table.getName());
                    rel2.setSourceColumn(fk2.getName());
                    rel2.setTargetColumn(fk1.getName());
                    rel2.setFieldName(pluralize(toFieldName(table1.getName())));
                    rel2.setTargetClassName(table1.getClassName());
                    table2.addRelationship(rel2);
                }
                continue;
            }
            
            // Process regular foreign keys
            for (Column col : table.getColumns()) {
                if (col.isForeignKey() && !col.isPrimaryKey()) {
                    Table referencedTable = tableMap.get(col.getReferencedTable());
                    if (referencedTable != null) {
                        // This is a @ManyToOne from current table to referenced table
                        Relationship manyToOne = new Relationship(RelationshipType.MANY_TO_ONE, table.getName(), referencedTable.getName());
                        manyToOne.setSourceColumn(col.getName());
                        manyToOne.setTargetColumn(col.getReferencedColumn());
                        manyToOne.setFieldName(toFieldName(referencedTable.getName()));
                        manyToOne.setTargetClassName(referencedTable.getClassName());
                        table.addRelationship(manyToOne);
                        
                        // Add inverse @OneToMany to referenced table
                        Relationship oneToMany = new Relationship(RelationshipType.ONE_TO_MANY, referencedTable.getName(), table.getName());
                        oneToMany.setMappedBy(toFieldName(referencedTable.getName()));
                        oneToMany.setFieldName(pluralize(toFieldName(table.getName())));
                        oneToMany.setTargetClassName(table.getClassName());
                        
                        // Check if this is actually one-to-one (FK is unique)
                        if (col.isUnique()) {
                            manyToOne.setType(RelationshipType.ONE_TO_ONE);
                            oneToMany.setType(RelationshipType.ONE_TO_ONE);
                            oneToMany.setFieldName(toFieldName(table.getName()));
                        }
                        
                        referencedTable.addRelationship(oneToMany);
                    }
                }
            }
        }
    }

    private String toClassName(String tableName) {
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = true;
        for (char c : tableName.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    sb.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        return sb.toString();
    }

    private String toFieldName(String colName) {
        String className = toClassName(colName);
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    private String pluralize(String word) {
        // Simple pluralization (can be enhanced)
        if (word.endsWith("s") || word.endsWith("x") || word.endsWith("z") || 
            word.endsWith("ch") || word.endsWith("sh")) {
            return word + "es";
        } else if (word.endsWith("y") && word.length() > 1 && !isVowel(word.charAt(word.length() - 2))) {
            return word.substring(0, word.length() - 1) + "ies";
        } else {
            return word + "s";
        }
    }

    private boolean isVowel(char c) {
        c = Character.toLowerCase(c);
        return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u';
    }

    private String mapToJavaType(String sqlType) {
        sqlType = sqlType.toUpperCase();
        if (sqlType.startsWith("VARCHAR") || sqlType.startsWith("TEXT") || sqlType.startsWith("CHAR")) return "String";
        if (sqlType.startsWith("INT") || sqlType.startsWith("INTEGER")) return "Integer";
        if (sqlType.startsWith("BIGINT")) return "Long";
        if (sqlType.startsWith("DOUBLE") || sqlType.startsWith("FLOAT")) return "Double";
        if (sqlType.startsWith("BOOLEAN") || sqlType.startsWith("BIT")) return "Boolean";
        if (sqlType.startsWith("DATE")) return "LocalDate";
        if (sqlType.startsWith("TIMESTAMP") || sqlType.startsWith("DATETIME")) return "LocalDateTime";
        return "String"; // Default
    }
}
