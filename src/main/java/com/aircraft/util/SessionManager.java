package com.aircraft.util;

import com.aircraft.model.User;

/**
 * Session manager utility class that handles user session information.
 * Implements a singleton pattern to maintain session across the application.
 */
public class SessionManager {
    // Singleton instance
    private static SessionManager instance;

    // Current logged-in user
    private User currentUser;

    /**
     * Private constructor to prevent instantiation from outside.
     */
    private SessionManager() {
        // Private constructor for singleton pattern
    }

    /**
     * Gets the singleton instance of the SessionManager.
     *
     * @return The SessionManager instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Gets the current logged-in user.
     *
     * @return The current User object
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Sets the current logged-in user.
     *
     * @param user The User object to set as current
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Clears the current user session (logout).
     */
    public void clearSession() {
        this.currentUser = null;
    }

    /**
     * Checks if a user is currently logged in.
     *
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Gets the username of the current logged-in user.
     *
     * @return The username of the current user, or null if no user is logged in
     */
    public String getCurrentUsername() {
        return isLoggedIn() ? currentUser.getUsername() : null;
    }
}