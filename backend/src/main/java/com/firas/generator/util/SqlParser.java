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

/**
 * Utility class for parsing SQL schemas and extracting table/column metadata.
 * 
 * This parser analyzes SQL CREATE TABLE and ALTER TABLE statements to extract:
 * - Table names and their columns
 * - Column data types and constraints (PRIMARY KEY, FOREIGN KEY, UNIQUE, AUTO_INCREMENT, etc.)
 * - Foreign key relationships between tables
 * - JPA relationship types (OneToOne, OneToMany, ManyToOne, ManyToMany)
 * 
 * The parser supports:
 * - CREATE TABLE statements with inline constraints
 * - ALTER TABLE statements for adding constraints, modifying columns, and dropping columns
 * - Foreign key detection (both inline and separate CONSTRAINT clauses)
 * - Join table detection for many-to-many relationships
 * - Automatic relationship inference based on foreign key constraints
 * 
 * The parsed metadata is used to generate JPA entity classes with proper annotations.
 * 
 * @author Firas Baklouti
 * @version 1.0
 * @since 2025-12-01
 */
@Component
public class SqlParser {

    /** Regex pattern for matching CREATE TABLE statements */
    private static final Pattern TABLE_PATTERN = Pattern.compile("CREATE\\s+TABLE\\s+`?(\\w+)`?\\s*\\(([^;]*?)\\)\\s*(?:ENGINE|CHARSET|COLLATE|;)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);    
    
    /** Regex pattern for matching ALTER TABLE statements */
    private static final Pattern ALTER_TABLE_PATTERN = Pattern.compile("ALTER\\s+TABLE\\s+`?(\\w+)`?\\s+(.*?);", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    /** Regex pattern for matching column definitions */
    private static final Pattern COLUMN_PATTERN = Pattern.compile("`?(\\w+)`?\\s+(\\w+)(\\(.*?\\))?\\s*(.*)");
    
    /** Regex pattern for matching FOREIGN KEY constraints */
    private static final Pattern FK_CONSTRAINT_PATTERN = Pattern.compile("FOREIGN\\s+KEY\\s*\\(`?(\\w+)`?\\)\\s*REFERENCES\\s+`?(\\w+)`?\\s*\\(`?(\\w+)`?\\)", Pattern.CASE_INSENSITIVE);
    
    /** Regex pattern for matching inline REFERENCES clauses */
    private static final Pattern INLINE_FK_PATTERN = Pattern.compile("REFERENCES\\s+`?(\\w+)`?\\s*\\(`?(\\w+)`?\\)", Pattern.CASE_INSENSITIVE);
    
    // Regex patterns for ALTER TABLE operations
    
    /** Pattern for detecting PRIMARY KEY additions in ALTER TABLE */
    private static final Pattern ALTER_PK_PATTERN = Pattern.compile("ADD\\s+PRIMARY\\s+KEY\\s*\\((.*?)\\)", Pattern.CASE_INSENSITIVE);
    
    /** Pattern for detecting FOREIGN KEY additions in ALTER TABLE */
    private static final Pattern ALTER_FK_PATTERN = Pattern.compile("ADD\\s+(?:CONSTRAINT\\s+`?(\\w+)`?\\s+)?FOREIGN\\s+KEY\\s*\\(`?(\\w+)`?\\)\\s*REFERENCES\\s+`?(\\w+)`?\\s*\\(`?(\\w+)`?\\)", Pattern.CASE_INSENSITIVE);
    
    /** Pattern for detecting AUTO_INCREMENT modifications in ALTER TABLE */
    private static final Pattern ALTER_AUTO_INCREMENT_PATTERN = Pattern.compile("MODIFY\\s+(?:COLUMN\\s+)?`?(\\w+)`?.*?AUTO_INCREMENT", Pattern.CASE_INSENSITIVE);
    
    /** Pattern for detecting UNIQUE constraint additions in ALTER TABLE */
    private static final Pattern ALTER_UNIQUE_PATTERN = Pattern.compile("ADD\\s+UNIQUE(?:\\s+(?:KEY|INDEX))?(?:\\s+`?(\\w+)`?)?\\s*\\((.*?)\\)", Pattern.CASE_INSENSITIVE);
    
    /** Pattern for detecting column drops in ALTER TABLE */
    private static final Pattern ALTER_DROP_COLUMN_PATTERN = Pattern.compile("DROP\\s+(?:COLUMN\\s+)?`?(?!(?:PRIMARY|FOREIGN|KEY|INDEX|CONSTRAINT)\\b)(\\w+)`?", Pattern.CASE_INSENSITIVE);

    /**
     * Parses SQL CREATE TABLE and ALTER TABLE statements to extract table metadata.
     * 
     * This method performs a three-pass parsing process:
     * 1. First pass: Parse CREATE TABLE statements to extract tables and columns
     * 2. Second pass: Parse ALTER TABLE statements to apply schema modifications
     * 3. Third pass: Build relationships between tables based on foreign keys
     * 
     * Join tables (for many-to-many relationships) are automatically detected and
     * excluded from the final result.
     * 
     * @param sql The SQL schema content containing CREATE TABLE and ALTER TABLE statements
     * @return List of Table objects with columns and relationships populated
     */
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
                    line.toUpperCase().startsWith("CONSTRAINT") || line.toUpperCase().startsWith("UNIQUE KEY") ||
                    line.toUpperCase().startsWith("KEY") || line.toUpperCase().startsWith("INDEX")) {
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

        // Second pass: parse ALTER TABLE statements
        Matcher alterMatcher = ALTER_TABLE_PATTERN.matcher(sql);
        while (alterMatcher.find()) {
            String tableName = alterMatcher.group(1);
            String alterBody = alterMatcher.group(2);

            Table table = tableMap.get(tableName);
            if (table != null) {
                // Handle Primary Keys
                Matcher pkMatcher = ALTER_PK_PATTERN.matcher(alterBody);
                while (pkMatcher.find()) {
                    String cols = pkMatcher.group(1);
                    String[] pkCols = cols.split(",");
                    for (String pkCol : pkCols) {
                        String cleanCol = pkCol.trim().replaceAll("`", "");
                        for (Column col : table.getColumns()) {
                            if (col.getName().equals(cleanCol)) {
                                col.setPrimaryKey(true);
                                break;
                            }
                        }
                    }
                }

                // Handle Auto Increment
                Matcher aiMatcher = ALTER_AUTO_INCREMENT_PATTERN.matcher(alterBody);
                while (aiMatcher.find()) {
                    String colName = aiMatcher.group(1);
                    for (Column col : table.getColumns()) {
                        if (col.getName().equals(colName)) {
                            col.setAutoIncrement(true);
                            break;
                        }
                    }
                }

                // Handle Unique Keys
                Matcher uniqueMatcher = ALTER_UNIQUE_PATTERN.matcher(alterBody);
                while (uniqueMatcher.find()) {
                    String cols = uniqueMatcher.group(2); // Group 2 has columns
                    if (cols != null) {
                        String[] uniqueCols = cols.split(",");
                        for (String uCol : uniqueCols) {
                            String cleanCol = uCol.trim().replaceAll("`", "");
                            for (Column col : table.getColumns()) {
                                if (col.getName().equals(cleanCol)) {
                                    col.setUnique(true);
                                    break;
                                }
                            }
                        }
                    }
                }

                // Handle Drop Column
                Matcher dropMatcher = ALTER_DROP_COLUMN_PATTERN.matcher(alterBody);
                while (dropMatcher.find()) {
                    String colName = dropMatcher.group(1);
                    table.getColumns().removeIf(col -> col.getName().equals(colName));
                }

                // Handle Foreign Keys
                Matcher fkMatcher = ALTER_FK_PATTERN.matcher(alterBody);
                while (fkMatcher.find()) {
                    // Group 1 is constraint name (optional), 2 is col name, 3 is ref table, 4 is ref col
                    String columnName = fkMatcher.group(2);
                    String refTable = fkMatcher.group(3);
                    String refColumn = fkMatcher.group(4);

                    for (Column col : table.getColumns()) {
                        if (col.getName().equals(columnName)) {
                            col.setForeignKey(true);
                            col.setReferencedTable(refTable);
                            col.setReferencedColumn(refColumn);
                            break;
                        }
                    }
                }
            }
        }
        
        // Third pass: build relationships
        buildRelationships(tables, tableMap);
        
        // Filter out join tables from the final list
        tables.removeIf(Table::isJoinTable);
        
        return tables;
    }

    /**
     * Builds JPA relationships between tables based on foreign key constraints.
     * 
     * This method analyzes foreign keys to determine relationship types:
     * - Detects join tables (tables with exactly 2 FKs) for many-to-many relationships
     * - Creates ManyToOne relationships for regular foreign keys
     * - Creates inverse OneToMany relationships on referenced tables
     * - Detects OneToOne relationships when foreign key has UNIQUE constraint
     * 
     * @param tables List of all tables
     * @param tableMap Map of table name to Table object for quick lookup
     */
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

    /**
     * Converts a database table name to a Java class name.
     * 
     * Converts snake_case to PascalCase (e.g., "user_profile" -> "UserProfile").
     * 
     * @param tableName The database table name
     * @return Java class name in PascalCase
     */
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

    /**
     * Converts a database column name to a Java field name.
     * 
     * Converts snake_case to camelCase (e.g., "user_id" -> "userId").
     * 
     * @param colName The database column name
     * @return Java field name in camelCase
     */
    private String toFieldName(String colName) {
        String className = toClassName(colName);
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    /**
     * Converts a singular word to its plural form.
     * 
     * Implements basic English pluralization rules:
     * - Words ending in s, x, z, ch, sh -> add "es"
     * - Words ending in consonant + y -> replace y with "ies"
     * - All other words -> add "s"
     * 
     * @param word The singular word
     * @return Pluralized form of the word
     */
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

    /**
     * Checks if a character is a vowel.
     * 
     * @param c The character to check
     * @return true if the character is a vowel (a, e, i, o, u)
     */
    private boolean isVowel(char c) {
        c = Character.toLowerCase(c);
        return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u';
    }

    /**
     * Maps SQL data types to corresponding Java types.
     * 
     * Supports common SQL types including:
     * - String types: VARCHAR, TEXT, CHAR
     * - Integer types: INT, INTEGER, BIGINT
     * - Decimal types: DOUBLE, FLOAT
     * - Boolean types: BOOLEAN, BIT
     * - Date/Time types: DATE, TIMESTAMP, DATETIME
     * 
     * @param sqlType The SQL data type
     * @return Corresponding Java type (defaults to "String" if unknown)
     */
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
