package com.aircraft.controller;

import com.aircraft.dao.UserDAO;
import com.aircraft.model.User;
import com.aircraft.util.AlertUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Objects;

/**
 * Controller for the signup screen of the application.
 * Handles user registration functionality.
 */
public class SignupController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button signupButton;

    @FXML
    private Hyperlink loginLink;

    private final UserDAO userDAO = new UserDAO();

    /**
     * Handles the signup button click event.
     * Validates user input and registers a new user.
     *
     * @param event The action event
     */
    @FXML
    protected void onSignupButtonClick(ActionEvent event) {
        Window owner = signupButton.getScene().getWindow();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate input fields
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            AlertUtils.showError(owner, "Validation Error", "Please fill in all fields");
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            AlertUtils.showError(owner, "Validation Error", "Passwords do not match");
            return;
        }

        // Check if username already exists
        if (userDAO.getUserByUsername(username) != null) {
            AlertUtils.showError(owner, "Validation Error", "Username already exists");
            return;
        }

        // Create and save the new user
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);

        try {
            boolean success = userDAO.insert(newUser);

            if (success) {
                AlertUtils.showInformation(owner, "Success", "Account created successfully");
                // Navigate to login screen
                navigateToLogin(event);
            } else {
                AlertUtils.showError(owner, "Error", "Failed to create account");
            }
        } catch (Exception e) {
            AlertUtils.showError(owner, "Error", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the login link click event.
     * Navigates back to the login screen.
     *
     * @param event The action event
     */
    @FXML
    protected void onLoginLinkClick(ActionEvent event) {
        navigateToLogin(event);
    }

    /**
     * Navigates to the login screen.
     *
     * @param event The ActionEvent object
     */
    private void navigateToLogin(ActionEvent event) {
        try {
            // Load the login FXML
            // Using a different approach to avoid any potential caching issues
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/login.fxml"));
            Parent loginParent = loader.load();

            // Get the current stage
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();

            // Create scene
            Scene loginScene = new Scene(loginParent);

            // Ensure CSS is applied
            loginScene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/css/login.css")).toExternalForm()
            );

            // Set the scene
            stage.setScene(loginScene);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            Window owner = loginLink.getScene().getWindow();
            AlertUtils.showError(owner, "Navigation Error", "Error navigating to login: " + e.getMessage());
            e.printStackTrace();
        }
    }
}