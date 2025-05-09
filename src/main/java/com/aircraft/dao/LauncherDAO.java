package com.aircraft.dao;

import com.aircraft.model.Launcher;
import com.aircraft.model.LauncherMission;
import com.aircraft.model.LauncherStatus;
import com.aircraft.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Launcher-related operations.
 */
public class LauncherDAO {

    /**
     * Retrieves a launcher by its part number.
     *
     * @param partNumber The part number to search for
     * @return The Launcher object or null if not found
     */
    public Launcher getByPartNumber(String partNumber) {
        Launcher launcher = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String query = "SELECT PartNumber, Nomenclatura, CodiceDitta, OreVitaOperativa " +
                    "FROM anagrafica_lanciatore " +
                    "WHERE PartNumber = ?";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, partNumber);

            rs = stmt.executeQuery();
            if (rs.next()) {
                launcher = new Launcher();
                launcher.setPartNumber(rs.getString("PartNumber"));
                launcher.setNomenclatura(rs.getString("Nomenclatura"));
                launcher.setCodiceDitta(rs.getString("CodiceDitta"));
                launcher.setOreVitaOperativa(rs.getBigDecimal("OreVitaOperativa"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }

        return launcher;
    }

    /**
     * Retrieves all launchers from the database.
     *
     * @return A list of all Launcher objects
     */
    public List<Launcher> getAll() {
        List<Launcher> launchers = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String query = "SELECT PartNumber, Nomenclatura, CodiceDitta, OreVitaOperativa " +
                    "FROM anagrafica_lanciatore " +
                    "ORDER BY PartNumber";

            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Launcher launcher = new Launcher();
                launcher.setPartNumber(rs.getString("PartNumber"));
                launcher.setNomenclatura(rs.getString("Nomenclatura"));
                launcher.setCodiceDitta(rs.getString("CodiceDitta"));
                launcher.setOreVitaOperativa(rs.getBigDecimal("OreVitaOperativa"));
                launchers.add(launcher);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }

        return launchers;
    }

    /**
     * Inserts a new launcher into the database.
     *
     * @param launcher The Launcher object to insert
     * @return true if successful, false otherwise
     */
    public boolean insert(Launcher launcher) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBUtil.getConnection();
            String query = "INSERT INTO anagrafica_lanciatore (PartNumber, Nomenclatura, CodiceDitta, OreVitaOperativa) " +
                    "VALUES (?, ?, ?, ?)";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, launcher.getPartNumber());
            stmt.setString(2, launcher.getNomenclatura());
            stmt.setString(3, launcher.getCodiceDitta());
            stmt.setBigDecimal(4, launcher.getOreVitaOperativa());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.closeResources(conn, stmt, null);
        }
    }

    /**
     * Updates an existing launcher in the database.
     *
     * @param launcher The Launcher object to update
     * @return true if successful, false otherwise
     */
    public boolean update(Launcher launcher) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBUtil.getConnection();
            String query = "UPDATE anagrafica_lanciatore " +
                    "SET Nomenclatura = ?, CodiceDitta = ?, OreVitaOperativa = ? " +
                    "WHERE PartNumber = ?";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, launcher.getNomenclatura());
            stmt.setString(2, launcher.getCodiceDitta());
            stmt.setBigDecimal(3, launcher.getOreVitaOperativa());
            stmt.setString(4, launcher.getPartNumber());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.closeResources(conn, stmt, null);
        }
    }

    /**
     * Deletes a launcher from the database by its part number.
     *
     * @param partNumber The part number of the launcher to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(String partNumber) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBUtil.getConnection();
            String query = "DELETE FROM anagrafica_lanciatore WHERE PartNumber = ?";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, partNumber);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.closeResources(conn, stmt, null);
        }
    }

    /**
     * Gets all launcher serial numbers from the database.
     *
     * @return A list of launcher serial numbers
     */
    public List<String> getAllLauncherSerialNumbers() {
        List<String> serialNumbers = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String query = "SELECT DISTINCT SerialNumber FROM storico_lanciatore ORDER BY SerialNumber";

            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                serialNumbers.add(rs.getString("SerialNumber"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }

        return serialNumbers;
    }

    /**
     * Gets the launcher status by its serial number from vista_stato_vita_lanciatore view.
     *
     * @param serialNumber The serial number to look up
     * @return The LauncherStatus object or null if not found
     */
    public LauncherStatus getLauncherStatusBySerialNumber(String serialNumber) {
        LauncherStatus status = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String query = "SELECT v.Nome_Lanciatore, v.Lanciatore_PartNumber, v.Lanciatore_SerialNumber, " +
                    "v.Numero_Missioni, v.Missioni_con_Sparo, v.Missioni_senza_Sparo, " +
                    "v.Ore_di_Volo_Totali, v.Vita_Residua_Percentuale, p.Stato_Manutentivo " +
                    "FROM vista_stato_vita_lanciatore v " +
                    "JOIN vista_predizione_manutenzione_lanciatore p " +
                    "ON v.Lanciatore_SerialNumber = p.Lanciatore_SerialNumber " +
                    "WHERE v.Lanciatore_SerialNumber = ?";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, serialNumber);

            rs = stmt.executeQuery();
            if (rs.next()) {
                status = new LauncherStatus();
                status.setLauncherName(rs.getString("Nome_Lanciatore"));
                status.setPartNumber(rs.getString("Lanciatore_PartNumber"));
                status.setSerialNumber(rs.getString("Lanciatore_SerialNumber"));
                status.setMissionCount(rs.getInt("Numero_Missioni"));
                status.setFiringCount(rs.getInt("Missioni_con_Sparo"));
                status.setNonFiringCount(rs.getInt("Missioni_senza_Sparo"));
                status.setFlightTime(rs.getDouble("Ore_di_Volo_Totali"));
                status.setRemainingLifePercentage(rs.getDouble("Vita_Residua_Percentuale"));
                status.setMaintenanceStatus(rs.getString("Stato_Manutentivo"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }

        return status;
    }

    /**
     * Gets mission history for a launcher by its serial number.
     *
     * @param serialNumber The serial number to look up
     * @return A list of LauncherMission objects
     */
    public List<LauncherMission> getMissionHistoryBySerialNumber(String serialNumber) {
        List<LauncherMission> missions = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String query = "SELECT m.ID as MissionId, m.DataMissione, m.MatricolaVelivolo as Aircraft, " +
                    "IFNULL(TIME_TO_SEC(TIMEDIFF(m.OraArrivo, m.OraPartenza)) / 3600, 0) as FlightTime, " +
                    "d.Danno_Miner_Missione as DamageFactor " +
                    "FROM vista_degrado_lanciatore_missione d " +
                    "JOIN missione m ON d.ID_Missione = m.ID " +
                    "WHERE d.Lanciatore_SerialNumber = ? " +
                    "ORDER BY m.DataMissione DESC";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, serialNumber);

            rs = stmt.executeQuery();
            while (rs.next()) {
                LauncherMission mission = new LauncherMission();
                mission.setMissionId(rs.getInt("MissionId"));
                mission.setMissionDate(rs.getDate("DataMissione").toLocalDate());
                mission.setAircraft(rs.getString("Aircraft"));
                mission.setFlightTime(rs.getDouble("FlightTime"));
                mission.setDamageFactor(rs.getDouble("DamageFactor"));
                mission.setLauncherSerialNumber(serialNumber);

                missions.add(mission);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }

        return missions;
    }
}