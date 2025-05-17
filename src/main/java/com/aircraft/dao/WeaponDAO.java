package com.aircraft.dao;

import com.aircraft.model.Weapon;
import com.aircraft.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Weapon-related database operations.
 * Provides methods for CRUD operations on weapons (anagrafica_carichi table).
 */
public class WeaponDAO {

    /**
     * Inserts a new weapon into the database.
     *
     * @param weapon The Weapon object to insert
     * @return true if insertion was successful, false otherwise
     */
    public boolean insert(Weapon weapon) {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();

            // SQL query to insert a new weapon
            String sql = "INSERT INTO anagrafica_carichi (PartNumber, Nomenclatura, CodiceDitta, Massa) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, weapon.getPartNumber());
            stmt.setString(2, weapon.getNomenclatura());
            stmt.setString(3, weapon.getCodiceDitta());

            if (weapon.getMassa() != null) {
                stmt.setBigDecimal(4, weapon.getMassa());
            } else {
                stmt.setNull(4, java.sql.Types.DECIMAL);
            }

            int rowsAffected = stmt.executeUpdate();
            success = rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting weapon: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, null);
        }

        return success;
    }

    /**
     * Updates an existing weapon in the database.
     *
     * @param weapon The Weapon object to update
     * @return true if update was successful, false otherwise
     */
    public boolean update(Weapon weapon) {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();

            // SQL query to update an existing weapon
            String sql = "UPDATE anagrafica_carichi SET Nomenclatura = ?, CodiceDitta = ?, Massa = ? WHERE PartNumber = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, weapon.getNomenclatura());
            stmt.setString(2, weapon.getCodiceDitta());

            if (weapon.getMassa() != null) {
                stmt.setBigDecimal(3, weapon.getMassa());
            } else {
                stmt.setNull(3, java.sql.Types.DECIMAL);
            }

            stmt.setString(4, weapon.getPartNumber());

            int rowsAffected = stmt.executeUpdate();
            success = rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating weapon: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, null);
        }

        return success;
    }

    /**
     * Deletes a weapon from the database by its part number.
     *
     * @param partNumber The part number of the weapon to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean delete(String partNumber) {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();

            // SQL query to delete a weapon
            String sql = "DELETE FROM anagrafica_carichi WHERE PartNumber = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, partNumber);

            int rowsAffected = stmt.executeUpdate();
            success = rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting weapon: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, null);
        }

        return success;
    }

    /**
     * Retrieves a weapon by its part number.
     *
     * @param partNumber The part number of the weapon to retrieve
     * @return The Weapon object if found, null otherwise
     */
    public Weapon getByPartNumber(String partNumber) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Weapon weapon = null;

        try {
            conn = DBUtil.getConnection();

            // SQL query to find a weapon by part number
            String sql = "SELECT * FROM anagrafica_carichi WHERE PartNumber = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, partNumber);

            rs = stmt.executeQuery();

            if (rs.next()) {
                // Weapon found, create and return Weapon object
                weapon = createWeaponFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving weapon: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }

        return weapon;
    }

    /**
     * Checks if a weapon with the given part number exists in the database.
     *
     * @param partNumber The part number to check
     * @return true if a weapon with the part number exists, false otherwise
     */
    public boolean existsByPartNumber(String partNumber) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean exists = false;

        try {
            conn = DBUtil.getConnection();

            // SQL query to check if a weapon exists with the given part number
            String sql = "SELECT 1 FROM anagrafica_carichi WHERE PartNumber = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, partNumber);

            rs = stmt.executeQuery();

            exists = rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking weapon existence: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }

        return exists;
    }

    /**
     * Retrieves all weapons from the database.
     *
     * @return A List of all Weapon objects
     */
    public List<Weapon> getAll() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Weapon> weapons = new ArrayList<>();

        try {
            conn = DBUtil.getConnection();

            // Use the view_weapon_list view instead of direct table access
            String sql = "SELECT * FROM view_weapon_list";
            stmt = conn.prepareStatement(sql);

            rs = stmt.executeQuery();

            while (rs.next()) {
                Weapon weapon = createWeaponFromResultSet(rs);
                weapons.add(weapon);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving weapons: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }

        return weapons;
    }

    /**
     * Creates a Weapon object from a ResultSet row.
     *
     * @param rs The ResultSet containing weapon data
     * @return A new Weapon object
     * @throws SQLException If there is an error accessing the ResultSet
     */
    private Weapon createWeaponFromResultSet(ResultSet rs) throws SQLException {
        Weapon weapon = new Weapon();
        weapon.setPartNumber(rs.getString("PartNumber"));
        weapon.setNomenclatura(rs.getString("Nomenclatura"));
        weapon.setCodiceDitta(rs.getString("CodiceDitta"));

        BigDecimal massa = rs.getBigDecimal("Massa");
        if (!rs.wasNull()) {
            weapon.setMassa(massa);
        }

        return weapon;
    }

    /**
     * Checks if a weapon with the given part number exists in the database.
     * Alias for existsByPartNumber.
     *
     * @param partNumber The part number to check
     * @return true if a weapon with the part number exists, false otherwise
     */
    public boolean exists(String partNumber) {
        return existsByPartNumber(partNumber);
    }

    /**
     * Checks if a weapon is available (not loaded on any aircraft).
     *
     * @param partNumber The part number of the weapon
     * @return true if available, false if already loaded
     */
    public boolean isWeaponAvailable(String partNumber) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        boolean isAvailable = true;

        try {
            connection = DBUtil.getConnection();

            // Check if weapon is loaded on any aircraft without an unload date
            String query = "SELECT COUNT(*) FROM storico_carico " +
                    "WHERE PartNumber = ? AND DataSbarco IS NULL";

            statement = connection.prepareStatement(query);
            statement.setString(1, partNumber);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                isAvailable = (count == 0); // Available if not loaded
            }

        } catch (SQLException e) {
            System.err.println("Error checking weapon availability: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(connection, statement, resultSet);
        }

        return isAvailable;
    }
}