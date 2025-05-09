package com.aircraft.model;

/**
 * Model class representing a user in the system.
 * Corresponds to the 'utenti' table in the database.
 */
public class User {
    private int id;
    private String username;
    private String password;

    /**
     * Default constructor.
     */
    public User() {
    }

    /**
     * Constructor with parameters.
     *
     * @param id User ID
     * @param username Username
     * @param password Password
     */
    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    /**
     * Gets the user ID.
     *
     * @return The user ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the user ID.
     *
     * @param id The user ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the username.
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username The username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password.
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password The password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns a string representation of the User object.
     *
     * @return A string representation of the User
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}