package com.aircraft;

import com.aircraft.util.DBUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Simple test class to verify database connection.
 * You can delete this after confirming the connection works.
 */
public class TestConnection {
    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            // Get a connection
            System.out.println("Attempting to connect to the database...");
            conn = DBUtil.getConnection();
            System.out.println("Connection successful!");

            // Create a statement
            stmt = conn.createStatement();

            // Execute a simple query
            rs = stmt.executeQuery("SELECT * FROM utenti");

            // Process the results
            System.out.println("Users in the database:");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") +
                        ", Username: " + rs.getString("username"));
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close resources
            DBUtil.closeResources(conn, stmt, rs);
        }
    }
}