package com.aircraft.dao;

import com.aircraft.model.Launcher;
import com.aircraft.model.LauncherLifeStatus;
import com.aircraft.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Launcher-related database operations.
 * Provides methods for CRUD operations on launchers and retrieving launcher status.
 */
public class LauncherDAO {

    /**
     * Inserts a new launcher into the database.
     *
     * @param launcher The Launcher object to insert
     * @return true if insertion was successful, false otherwise
     */
    public boolean insert(Launcher launcher) {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();

            // SQL query to insert a new launcher
            String sql = "INSERT INTO anagrafica_lanciatore (PartNumber, Nomenclatura, CodiceDitta, OreVitaOperativa) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, launcher.getPartNumber());
            stmt.setString(2, launcher.getNomenclatura());
            stmt.setString(3, launcher.getCodiceDitta());
            stmt.setBigDecimal(4, launcher.getOreVitaOperativa());

            int rowsAffected = stmt.executeUpdate();
            success = rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting launcher: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, null);
        }

        return success;
    }

    /**
     * Updates an existing launcher in the database.
     *
     * @param launcher The Launcher object to update
     * @return true if update was successful, false otherwise
     */
    public boolean update(Launcher launcher) {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();

            // SQL query to update an existing launcher
            String sql = "UPDATE anagrafica_lanciatore SET Nomenclatura = ?, CodiceDitta = ?, OreVitaOperativa = ? WHERE PartNumber = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, launcher.getNomenclatura());
            stmt.setString(2, launcher.getCodiceDitta());
            stmt.setBigDecimal(3, launcher.getOreVitaOperativa());
            stmt.setString(4, launcher.getPartNumber());

            int rowsAffected = stmt.executeUpdate();
            success = rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating launcher: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, null);
        }

        return success;
    }

    /**
     * Deletes a launcher from the database by its part number.
     *
     * @param partNumber The part number of the launcher to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean delete(String partNumber) {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();

            // SQL query to delete a launcher
            String sql = "DELETE FROM anagrafica_lanciatore WHERE PartNumber = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, partNumber);

            int rowsAffected = stmt.executeUpdate();
            success = rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting launcher: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, null);
        }

        return success;
    }

    /**
     * Retrieves a launcher by its part number.
     *
     * @param partNumber The part number of the launcher to retrieve
     * @return The Launcher object if found, null otherwise
     */
    public Launcher getByPartNumber(String partNumber) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Launcher launcher = null;

        try {
            conn = DBUtil.getConnection();

            // SQL query to find a launcher by part number
            String sql = "SELECT * FROM anagrafica_lanciatore WHERE PartNumber = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, partNumber);

            rs = stmt.executeQuery();

            if (rs.next()) {
                // Launcher found, create and return Launcher object
                launcher = new Launcher();
                launcher.setPartNumber(rs.getString("PartNumber"));
                launcher.setNomenclatura(rs.getString("Nomenclatura"));
                launcher.setCodiceDitta(rs.getString("CodiceDitta"));
                launcher.setOreVitaOperativa(rs.getBigDecimal("OreVitaOperativa"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving launcher: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }

        return launcher;
    }

    /**
     * Retrieves all launchers from the database.
     *
     * @return A List of all Launcher objects
     */
    public List<Launcher> getAll() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Launcher> launchers = new ArrayList<>();

        try {
            conn = DBUtil.getConnection();

            // SQL query to retrieve all launchers
            String sql = "SELECT * FROM anagrafica_lanciatore";
            stmt = conn.prepareStatement(sql);

            rs = stmt.executeQuery();

            while (rs.next()) {
                // For each row, create a Launcher object and add to list
                Launcher launcher = new Launcher();
                launcher.setPartNumber(rs.getString("PartNumber"));
                launcher.setNomenclatura(rs.getString("Nomenclatura"));
                launcher.setCodiceDitta(rs.getString("CodiceDitta"));
                launcher.setOreVitaOperativa(rs.getBigDecimal("OreVitaOperativa"));

                launchers.add(launcher);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving launchers: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }

        return launchers;
    }

    /**
     * Checks if a launcher with the given part number exists in the database.
     *
     * @param partNumber The part number to check
     * @return true if the launcher exists, false otherwise
     */
    public boolean exists(String partNumber) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean exists = false;

        try {
            conn = DBUtil.getConnection();

            // SQL query to check if a launcher exists
            String sql = "SELECT 1 FROM anagrafica_lanciatore WHERE PartNumber = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, partNumber);

            rs = stmt.executeQuery();

            exists = rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking launcher existence: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }

        return exists;
    }

    /**
     * Gets the life status of a launcher by its serial number.
     *
     * @param serialNumber The serial number of the launcher
     * @return The LauncherLifeStatus object if found, null otherwise
     */
    public LauncherLifeStatus getLauncherLifeStatus(String serialNumber) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        LauncherLifeStatus status = null;

        try {
            conn = DBUtil.getConnection();

            // SQL query to retrieve launcher life status from the view
            String sql = "SELECT * FROM vista_stato_vita_lanciatore WHERE Lanciatore_SerialNumber = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, serialNumber);

            rs = stmt.executeQuery();

            if (rs.next()) {
                // Status found, create and return LauncherLifeStatus object
                status = new LauncherLifeStatus();
                status.setNomeLanciatore(rs.getString("Nome_Lanciatore"));
                status.setPartNumber(rs.getString("Lanciatore_PartNumber"));
                status.setSerialNumber(rs.getString("Lanciatore_SerialNumber"));
                status.setNumeroMissioni(rs.getInt("Numero_Missioni"));
                status.setMissioniConSparo(rs.getInt("Missioni_con_Sparo"));
                status.setMissioniSenzaSparo(rs.getInt("Missioni_senza_Sparo"));
                status.setOreVoloTotali(rs.getBigDecimal("Ore_di_Volo_Totali"));
                status.setVitaResiduaPercentuale(rs.getDouble("Vita_Residua_Percentuale"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving launcher life status: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }

        return status;
    }
}