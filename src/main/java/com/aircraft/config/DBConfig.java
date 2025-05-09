package com.aircraft.config;

/**
 * Configuration class for database connection parameters.
 */
public class DBConfig {
    // JDBC URL for MySQL database connection
    public static final String JDBC_URL = "jdbc:mysql://localhost:3306/manutenzione_am?useSSL=false&serverTimezone=UTC";

    // Database username - replace with your MySQL username if not root
    public static final String USERNAME = "root";

    // Database password - replace with your MySQL password
    public static final String PASSWORD = "100K";

    // JDBC driver class name
    public static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
}