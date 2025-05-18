package com.aircraft.dao;

import com.aircraft.model.WeaponStatus;
import com.aircraft.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object extension for mission weapons and mission status information.
 * This includes methods for retrieving and updating weapon status for missions.
 */
public class MissionWeaponsDAO {

    /**
     * Retrieves weapons for a specific mission.
     * Shows launcher and missile information along with status.
     *
     * @param id The mission ID
     * @return A List of WeaponStatus objects for the specified mission
     */
    public List<WeaponStatus> getWeaponsForMission(int id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<WeaponStatus> weapons = new ArrayList<>();

        try {
            conn = DBUtil.getConnection();
            System.out.println("Fetching weapons for mission ID: " + id);

            // Get mission details first
            String sqlMission = "SELECT MatricolaVelivolo FROM missione WHERE ID = ?";
            stmt = conn.prepareStatement(sqlMission);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            String matricola = null;
            if (rs.next()) {
                matricola = rs.getString("MatricolaVelivolo");
                rs.close();
                stmt.close();
            } else {
                return weapons; // Return empty list if mission not found
            }

            // Use only columns that actually exist in your database tables
            String sqlWeapons =
                "SELECT sl.PosizioneVelivolo, sl.PartNumber AS LauncherPN, '' AS LauncherSN, " +
                "sc.PartNumber AS MissilePN, sc.Nomenclatura AS MissileName, " +
                "'ONBOARD' AS Status " +
                "FROM storico_lanciatore sl " +
                "LEFT JOIN storico_carico sc ON sl.PosizioneVelivolo = sc.PosizioneVelivolo " +
                "AND sl.MatricolaVelivolo = sc.MatricolaVelivolo " +
                "WHERE sl.MatricolaVelivolo = ? " +
                "ORDER BY sl.PosizioneVelivolo";

            stmt = conn.prepareStatement(sqlWeapons);
            stmt.setString(1, matricola);
            rs = stmt.executeQuery();

            while (rs.next()) {
                WeaponStatus weapon = new WeaponStatus();
                String position = rs.getString("PosizioneVelivolo");
                weapon.setPosition(position);
                weapon.setMissilePartNumber(rs.getString("MissilePN"));
                weapon.setMissileName(rs.getString("MissileName"));
                weapon.setStatus(rs.getString("Status"));
                weapon.setLauncherPartNumber(rs.getString("LauncherPN"));
                weapon.setLauncherSerialNumber(rs.getString("LauncherSN"));
                weapons.add(weapon);
            }

            System.out.println("Found " + weapons.size() + " weapons for mission ID: " + id);
        } catch (SQLException e) {
            System.err.println("Error retrieving weapons: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }

        return weapons;
    }

    /**
     * Retrieves missile firing status from dichiarazione_missile_gui.
     *
     * @param missionId The mission ID
     * @return A list of positions and their firing status
     */
    public List<String[]> getMissileFiringStatus(int missionId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<String[]> firingStatus = new ArrayList<>();

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT PosizioneVelivolo, Missile_Sparato FROM dichiarazione_missile_gui " +
                    "WHERE ID_Missione = ? ORDER BY PosizioneVelivolo";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, missionId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String[] status = new String[2];
                status[0] = rs.getString("PosizioneVelivolo");
                status[1] = rs.getString("Missile_Sparato");
                firingStatus.add(status);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving missile firing status: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }

        return firingStatus;
    }
}