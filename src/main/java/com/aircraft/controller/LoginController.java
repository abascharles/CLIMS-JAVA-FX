package com.aircraft.controller;

import com.aircraft.util.AlertUtils;
import com.aircraft.util.FXMLUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the login screen of the application.
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink signUpLink;

    /**
     * Handles the login button click event.
     * Validates user credentials and navigates to the dashboard on success.
     *
     * @param event The action event
     */
    @FXML
    protected void onLoginButtonClick(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Validate input fields
        if (username.isEmpty() || password.isEmpty()) {
            AlertUtils.showError(loginButton.getScene().getWindow(),
                    "Login Error", "Please enter both username and password.");
            return;
        }

        // In a real application, you would validate credentials against a database
        // For this example, we'll use a simple hardcoded check
        if (username.equals("admin") && password.equals("admin")) {
            try {
                // Load the dashboard scene
                Parent dashboardRoot = FXMLUtils.loadFXML("dashboard");
                Scene dashboardScene = new Scene(dashboardRoot);

                // Get the current stage
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                // Set the new scene
                currentStage.setScene(dashboardScene);
                currentStage.centerOnScreen();

            } catch (IOException e) {
                AlertUtils.showError(loginButton.getScene().getWindow(),
                        "Navigation Error", "Error loading dashboard: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            AlertUtils.showError(loginButton.getScene().getWindow(),
                    "Login Error", "Invalid username or password.");
        }
    }

    /**
     * Handles the sign up link click event.
     * Navigates to the sign up screen.
     *
     * @param event The action event
     */
    @FXML
    protected void onSignUpLinkClick(ActionEvent event) {
        try {
            // Load the signup scene
            Parent signupRoot = FXMLUtils.loadFXML("signup");
            Scene signupScene = new Scene(signupRoot);

            // Get the current stage
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the new scene
            currentStage.setScene(signupScene);
            currentStage.centerOnScreen();

        } catch (IOException e) {
            AlertUtils.showError(signUpLink.getScene().getWindow(),
                    "Navigation Error", "Error loading signup page: " + e.getMessage());
            e.printStackTrace();
        }
    }
}