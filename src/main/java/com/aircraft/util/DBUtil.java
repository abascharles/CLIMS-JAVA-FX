package com.aircraft.util;

import com.aircraft.config.DBConfig;

import java.sql.*;

/**
 * Utility class for database operations.
 */
public class DBUtil {

    /**
     * Gets a connection to the database.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load the JDBC driver
            Class.forName(DBConfig.DRIVER_CLASS);

            // Return a connection to the database
            return DriverManager.getConnection(
                    DBConfig.JDBC_URL,
                    DBConfig.USERNAME,
                    DBConfig.PASSWORD
            );
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
            throw new SQLException("JDBC Driver not found", e);
        }
    }

    /**
     * Closes database resources safely.
     */
    public static void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            System.err.println("Error closing ResultSet: " + e.getMessage());
        }

        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            System.err.println("Error closing Statement: " + e.getMessage());
        }

        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing Connection: " + e.getMessage());
        }
    }
}