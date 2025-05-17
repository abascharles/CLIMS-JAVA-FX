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
            String query = "SELECT PartNumber, Nomenclatura, DataInstallazione, DataRimozione, " +
                    "PosizioneVelivolo, MatricolaVelivolo, TipoComponente " +
                    "FROM views_material_handling " +
                    "WHERE PartNumber = ? " +
                    "ORDER BY DataInstallazione DESC";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, partNumber);

            rs = stmt.executeQuery();
            while (rs.next()) {
                MovementHistory history = new MovementHistory();
                history.setPartNumber(rs.getString("PartNumber"));
                history.setItemName(rs.getString("Nomenclatura"));
                history.setItemType(rs.getString("TipoComponente"));

                // Handle date conversion from SQL Date to LocalDate
                java.sql.Date installDate = rs.getDate("DataInstallazione");
                if (installDate != null) {
                    history.setDate(installDate.toLocalDate());
                }

                // Determine action type based on dates
                java.sql.Date removalDate = rs.getDate("DataRimozione");
                String actionType = (removalDate == null) ? "Embarkation" : "Disembarkation";
                history.setActionType(actionType);

                history.setLocation(rs.getString("PosizioneVelivolo"));
                history.setAircraftId(rs.getString("MatricolaVelivolo"));

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
     * Retrieves launcher movement history records for a specific part number.
     * Uses the views_material_handling view with TipoComponente = 'Lanciatore'.
     *
     * @param partNumber The part number to search for
     * @return List of MovementHistory objects for launchers
     */
    public List<MovementHistory> getLauncherHistoryByPartNumber(String partNumber) {
        List<MovementHistory> historyList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String query = "SELECT PartNumber, Nomenclatura, DataInstallazione, DataRimozione, " +
                    "PosizioneVelivolo, MatricolaVelivolo, TipoComponente " +
                    "FROM views_material_handling " +
                    "WHERE PartNumber = ? AND TipoComponente = 'Lanciatore' " +
                    "ORDER BY DataInstallazione DESC";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, partNumber);

            rs = stmt.executeQuery();
            while (rs.next()) {
                MovementHistory history = new MovementHistory();
                history.setPartNumber(rs.getString("PartNumber"));
                history.setItemName(rs.getString("Nomenclatura"));
                history.setItemType(rs.getString("TipoComponente"));

                // Determine serialNumber from other fields if needed or set to null
                // For now, leaving this blank since it's not in the view
                history.setSerialNumber("");

                // Handle date conversion from SQL Date to LocalDate
                java.sql.Date installDate = rs.getDate("DataInstallazione");
                if (installDate != null) {
                    history.setDate(installDate.toLocalDate());
                }

                // Determine action type based on dates
                java.sql.Date removalDate = rs.getDate("DataRimozione");
                String actionType = (removalDate == null) ? "Embarkation" : "Disembarkation";
                history.setActionType(actionType);

                history.setLocation(rs.getString("PosizioneVelivolo"));
                history.setAircraftId(rs.getString("MatricolaVelivolo"));

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
     * Retrieves missile/load movement history records for a specific part number.
     * Uses the views_material_handling view with TipoComponente = 'Carico'.
     *
     * @param partNumber The part number to search for
     * @return List of MovementHistory objects for missiles/loads
     */
    public List<MovementHistory> getLoadHistoryByPartNumber(String partNumber) {
        List<MovementHistory> historyList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String query = "SELECT PartNumber, Nomenclatura, DataInstallazione, DataRimozione, " +
                    "PosizioneVelivolo, MatricolaVelivolo, TipoComponente " +
                    "FROM views_material_handling " +
                    "WHERE PartNumber = ? AND TipoComponente = 'Carico' " +
                    "ORDER BY DataInstallazione DESC";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, partNumber);

            rs = stmt.executeQuery();
            while (rs.next()) {
                MovementHistory history = new MovementHistory();
                history.setPartNumber(rs.getString("PartNumber"));
                history.setItemName(rs.getString("Nomenclatura"));
                history.setItemType("Missile"); // Use "Missile" instead of "Carico" for UI consistency

                // Handle date conversion from SQL Date to LocalDate
                java.sql.Date installDate = rs.getDate("DataInstallazione");
                if (installDate != null) {
                    history.setDate(installDate.toLocalDate());
                }

                // Determine action type based on dates
                java.sql.Date removalDate = rs.getDate("DataRimozione");
                String actionType = (removalDate == null) ? "Embarkation" : "Disembarkation";
                history.setActionType(actionType);

                history.setLocation(rs.getString("PosizioneVelivolo"));
                history.setAircraftId(rs.getString("MatricolaVelivolo"));

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
     * Uses the views_material_handling view.
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
            String query = "SELECT DISTINCT TipoComponente FROM views_material_handling " +
                    "WHERE PartNumber = ? LIMIT 1";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, partNumber);

            rs = stmt.executeQuery();
            if (rs.next()) {
                String dbType = rs.getString("TipoComponente");
                // Convert to the types used in the UI
                if ("Lanciatore".equals(dbType) || "Launcher".equals(dbType)) {
                    return "Launcher";
                } else if ("Carico".equals(dbType)) {
                    return "Missile";
                }
                return dbType; // Return the type as is if it doesn't match known types
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
     * Uses the views_material_handling view.
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
            String query = "SELECT DISTINCT Nomenclatura FROM views_material_handling " +
                    "WHERE PartNumber = ? LIMIT 1";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, partNumber);

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

    /**
     * Inserts a new movement record.
     * This is just a placeholder as the application uses this method, but we should
     * modify the implementation if needed based on the new database design.
     */
    public boolean insertMovementRecord(String partNumber, String itemType, String itemName,
                                        String serialNumber, LocalDate actionDate,
                                        String actionType, String location, String aircraftId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();

            // This would need to be updated to insert into the appropriate tables
            // based on the type (storico_carico or storico_lanciatore)
            if ("Launcher".equals(itemType)) {
                // Insert into storico_lanciatore
                String query = "INSERT INTO storico_lanciatore (MatricolaVelivolo, PartNumber, " +
                        "DataInstallazione, DataRimozione, PosizioneVelivolo) " +
                        "VALUES (?, ?, ?, ?, ?)";

                stmt = conn.prepareStatement(query);
                stmt.setString(1, aircraftId);
                stmt.setString(2, partNumber);
                stmt.setDate(3, java.sql.Date.valueOf(actionDate));
                // If disembarkation, set removal date
                if ("Disembarkation".equals(actionType)) {
                    stmt.setDate(4, java.sql.Date.valueOf(actionDate));
                } else {
                    stmt.setNull(4, java.sql.Types.DATE);
                }
                stmt.setString(5, location);

            } else if ("Missile".equals(itemType)) {
                // Insert into storico_carico
                String query = "INSERT INTO storico_carico (PartNumber, Nomenclatura, " +
                        "DataImbarco, DataSbarco, PosizioneVelivolo, MatricolaVelivolo) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

                stmt = conn.prepareStatement(query);
                stmt.setString(1, partNumber);
                stmt.setString(2, itemName);
                stmt.setDate(3, java.sql.Date.valueOf(actionDate));
                // If disembarkation, set removal date
                if ("Disembarkation".equals(actionType)) {
                    stmt.setDate(4, java.sql.Date.valueOf(actionDate));
                } else {
                    stmt.setNull(4, java.sql.Types.DATE);
                }
                stmt.setString(5, location);
                stmt.setString(6, aircraftId);
            }

            if (stmt != null) {
                int rowsAffected = stmt.executeUpdate();
                success = rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, null);
        }

        return success;
    }
}