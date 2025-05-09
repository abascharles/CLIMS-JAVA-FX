package com.aircraft.dao;

import com.aircraft.model.RecordedData;
import com.aircraft.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for RecordedData-related database operations.
 * Provides methods for CRUD operations on flight recorded data.
 */
public class RecordedDataDAO {

    /**
     * Inserts new recorded data into the database.
     *
     * @param recordedData The RecordedData object to insert
     * @return true if insertion was successful, false otherwise
     */
    public boolean insert(RecordedData recordedData) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();

            // SQL query to insert new recorded data
            String sql = "INSERT INTO dati_registrati (MatricolaVelivolo, NumeroVolo, GloadMax, GloadMin, " +
                    "QuotaMedia, VelocitaMassima, StatoMissili, StatoElaborato) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, recordedData.getMatricolaVelivolo());
            stmt.setInt(2, recordedData.getNumeroVolo());
            stmt.setBigDecimal(3, recordedData.getGloadMax());
            stmt.setBigDecimal(4, recordedData.getGloadMin());
            stmt.setInt(5, recordedData.getQuotaMedia());
            stmt.setInt(6, recordedData.getVelocitaMassima());
            stmt.setString(7, recordedData.getStatoMissili());
            stmt.setBoolean(8, recordedData.isStatoElaborato());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Get the generated ID
                generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    recordedData.setId(generatedKeys.getInt(1));
                    success = true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting recorded data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, generatedKeys);
        }

        return success;
    }

    /**
     * Updates existing recorded data in the database.
     *
     * @param recordedData The RecordedData object to update
     * @return true if update was successful, false otherwise
     */
    public boolean update(RecordedData recordedData) {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();

            // SQL query to update existing recorded data
            String sql = "UPDATE dati_registrati SET MatricolaVelivolo = ?, NumeroVolo = ?, " +
                    "GloadMax = ?, GloadMin = ?, QuotaMedia = ?, VelocitaMassima = ?, " +
                    "StatoMissili = ?, StatoElaborato = ? WHERE ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, recordedData.getMatricolaVelivolo());
            stmt.setInt(2, recordedData.getNumeroVolo());
            stmt.setBigDecimal(3, recordedData.getGloadMax());
            stmt.setBigDecimal(4, recordedData.getGloadMin());
            stmt.setInt(5, recordedData.getQuotaMedia());
            stmt.setInt(6, recordedData.getVelocitaMassima());
            stmt.setString(7, recordedData.getStatoMissili());
            stmt.setBoolean(8, recordedData.isStatoElaborato());
            stmt.setInt(9, recordedData.getId());

            int rowsAffected = stmt.executeUpdate();
            success = rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating recorded data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, null);
        }

        return success;
    }

    /**
     * Deletes recorded data from the database by its ID.
     *
     * @param id The ID of the recorded data to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean delete(int id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();

            // SQL query to delete recorded data
            String sql = "DELETE FROM dati_registrati WHERE ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            success = rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting recorded data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, null);
        }

        return success;
    }

    /**
     * Retrieves recorded data by its ID.
     *
     * @param id The ID of the recorded data to retrieve
     * @return The RecordedData object if found, null otherwise
     */
    public RecordedData getById(int id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        RecordedData recordedData = null;

        try {
            conn = DBUtil.getConnection();

            // SQL query to find recorded data by ID
            String sql = "SELECT * FROM dati_registrati WHERE ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            rs = stmt.executeQuery();

            if (rs.next()) {
                // Recorded data found, create and return RecordedData object
                recordedData = createRecordedDataFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving recorded data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }

        return recordedData;
    }

    /**
     * Retrieves recorded data by aircraft and flight number.
     *
     * @param matricolaVelivolo The aircraft serial number
     * @param numeroVolo The flight number
     * @return The RecordedData object if found, null otherwise
     */
    public RecordedData getByFlightNumber(String matricolaVelivolo, Integer numeroVolo) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        RecordedData recordedData = null;

        try {
            conn = DBUtil.getConnection();

            // SQL query to find recorded data by aircraft and flight number
            String sql = "SELECT * FROM dati_registrati WHERE MatricolaVelivolo = ? AND NumeroVolo = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, matricolaVelivolo);
            stmt.setInt(2, numeroVolo);

            rs = stmt.executeQuery();

            if (rs.next()) {
                // Recorded data found, create and return RecordedData object
                recordedData = createRecordedDataFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving recorded data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }

        return recordedData;
    }

    /**
     * Retrieves all recorded data from the database.
     *
     * @return A List of all RecordedData objects
     */
    public List<RecordedData> getAll() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<RecordedData> recordedDataList = new ArrayList<>();

        try {
            conn = DBUtil.getConnection();

            // SQL query to retrieve all recorded data
            String sql = "SELECT * FROM dati_registrati";
            stmt = conn.prepareStatement(sql);

            rs = stmt.executeQuery();

            while (rs.next()) {
                // For each row, create a RecordedData object and add to list
                RecordedData recordedData = createRecordedDataFromResultSet(rs);
                recordedDataList.add(recordedData);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving recorded data list: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }

        return recordedDataList;
    }

    /**
     * Creates a RecordedData object from a ResultSet row.
     *
     * @param rs The ResultSet containing recorded data
     * @return A new RecordedData object
     * @throws SQLException If there is an error accessing the ResultSet
     */
    private RecordedData createRecordedDataFromResultSet(ResultSet rs) throws SQLException {
        RecordedData recordedData = new RecordedData();
        recordedData.setId(rs.getInt("ID"));
        recordedData.setMatricolaVelivolo(rs.getString("MatricolaVelivolo"));
        recordedData.setNumeroVolo(rs.getInt("NumeroVolo"));
        recordedData.setGloadMax(rs.getBigDecimal("GloadMax"));
        recordedData.setGloadMin(rs.getBigDecimal("GloadMin"));
        recordedData.setQuotaMedia(rs.getInt("QuotaMedia"));
        recordedData.setVelocitaMassima(rs.getInt("VelocitaMassima"));
        recordedData.setStatoMissili(rs.getString("StatoMissili"));
        recordedData.setStatoElaborato(rs.getBoolean("StatoElaborato"));
        return recordedData;
    }
}