package com.firas.generator.util.sql.implementation;

import com.firas.generator.util.sql.SqlConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MysqlConnection implements SqlConnection {
    // Railway MySQL environment variables - try multiple variable names for compatibility
    private final String host;
    private final String port;
    private final String user;
    private final String pass;
    private final String dbName;

    public MysqlConnection() {
        // Try different environment variable formats that Railway might use
        this.host = getEnvWithFallback("MYSQLHOST", "MYSQL_HOST", "mysql.railway.internal");
        this.port = getEnvWithFallback("MYSQLPORT", "MYSQL_PORT", "3306");
        this.user = getEnvWithFallback("MYSQLUSER", "MYSQL_USER", "root");
        this.pass = getEnvWithFallback("MYSQLPASSWORD", "MYSQL_PASSWORD", "MYSQL_ROOT_PASSWORD");
        this.dbName = getEnvWithFallback("MYSQLDATABASE", "MYSQL_DATABASE", "railway");

        // Debug logging to help troubleshoot
        System.out.println("MySQL Connection Config:");
        System.out.println("  Host: " + this.host);
        System.out.println("  Port: " + this.port);
        System.out.println("  User: " + this.user);
        System.out.println("  Database: " + this.dbName);
        System.out.println("  RAILWAY_ENVIRONMENT: " + System.getenv("RAILWAY_ENVIRONMENT"));
    }

    private static String getEnvWithFallback(String primary, String secondary, String defaultVal) {
        String value = System.getenv(primary);
        if (value != null && !value.isEmpty()) return value;

        value = System.getenv(secondary);
        if (value != null && !value.isEmpty()) return value;

        // Check if defaultVal is actually another env var name
        value = System.getenv(defaultVal);
        if (value != null && !value.isEmpty()) return value;

        return defaultVal;
    }

    @Override
    public Connection getConnection(String sql) throws SQLException {

        // 1. Drop/Create DB using root connection (without try-with-resources on final conn)
        String adminUrl =
                "jdbc:mysql://" + host + ":" + port + "/?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        try (Connection rootConn = DriverManager.getConnection(adminUrl, user, pass)) {
            rootConn.prepareStatement("DROP DATABASE IF EXISTS " + dbName).execute();
            rootConn.prepareStatement("CREATE DATABASE " + dbName).execute();
        }

        // 2. Open connection to new DB (IMPORTANT: do NOT auto-close it)
        String url =
                "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        Connection conn = DriverManager.getConnection(url, user, pass);

        // 3. Execute SQL dump
        for (String stmt : sql.split(";")) {
            String trimmed = stmt.trim();
            if (!trimmed.isEmpty()) {
                try {
                    conn.prepareStatement(trimmed).execute();
                } catch (SQLException e) {
                    System.err.println("SQL Exec error: " + trimmed);
                    e.printStackTrace();
                }
            }
        }

        // 4. Return the OPEN connection
        return conn;
    }
}
