package com.aircraft.dao;

import com.aircraft.model.MovementHistory;
import com.aircraft.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for MovementHistory operations.
 */
public class MovementHistoryDAO {

    /**
     * Retrieves all movement history records for a specific part number.
     *
     * @param partNumber The part number to search for
     * @return List of MovementHistory objects
     */
    public List<MovementHistory> getByPartNumber(String partNumber) {
        List<MovementHistory> historyList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String query = "SELECT part_number, item_type, item_name, serial_number, " +
                    "action_date, action_type, location, aircraft_id " +
                    "FROM vista_movement_history " +
                    "WHERE part_number = ? " +
                    "ORDER BY action_date DESC";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, partNumber);

            rs = stmt.executeQuery();
            while (rs.next()) {
                MovementHistory history = new MovementHistory();
                history.setPartNumber(rs.getString("part_number"));
                history.setItemType(rs.getString("item_type"));
                history.setItemName(rs.getString("item_name"));
                history.setSerialNumber(rs.getString("serial_number"));

                // Handle date conversion from SQL Date to LocalDate
                java.sql.Date sqlDate = rs.getDate("action_date");
                if (sqlDate != null) {
                    history.setDate(sqlDate.toLocalDate());
                }

                history.setActionType(rs.getString("action_type"));
                history.setLocation(rs.getString("location"));
                history.setAircraftId(rs.getString("aircraft_id"));

                historyList.add(history);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }

        return historyList;
    }

    /**
     * Determines if a part number exists in the system and what type it is.
     *
     * @param partNumber The part number to check
     * @return The item type ("Launcher", "Missile") or null if not found
     */
    public String getItemTypeByPartNumber(String partNumber) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            // First check in anagrafica_lanciatore
            String query = "SELECT 'Launcher' AS item_type FROM anagrafica_lanciatore WHERE PartNumber = ? " +
                    "UNION " +
                    "SELECT 'Missile' AS item_type FROM anagrafica_carichi WHERE PartNumber = ? " +
                    "LIMIT 1";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, partNumber);
            stmt.setString(2, partNumber);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("item_type");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }

        return null;
    }

    /**
     * Gets the item name (nomenclatura) for a part number.
     *
     * @param partNumber The part number to look up
     * @return The item name or null if not found
     */
    public String getItemNameByPartNumber(String partNumber) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            // Check in both launcher and cargo tables
            String query = "SELECT Nomenclatura FROM anagrafica_lanciatore WHERE PartNumber = ? " +
                    "UNION " +
                    "SELECT Nomenclatura FROM anagrafica_carichi WHERE PartNumber = ? " +
                    "LIMIT 1";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, partNumber);
            stmt.setString(2, partNumber);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("Nomenclatura");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }

        return null;
    }
}