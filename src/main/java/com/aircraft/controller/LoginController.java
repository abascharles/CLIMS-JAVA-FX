package com.aircraft.controller;

import com.aircraft.dao.UserDAO;
import com.aircraft.model.User;
import com.aircraft.util.AlertUtils;
import com.aircraft.util.SessionManager;
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

    private final UserDAO userDAO = new UserDAO();

    /**
     * Handles the login button click event.
     * Validates user credentials and navigates to the dashboard on success.
     *
     * @param event The action event
     */
    @FXML
    protected void onLoginButtonClick(ActionEvent event) {
        Window owner = loginButton.getScene().getWindow();
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Validate input fields
        if (username.isEmpty() || password.isEmpty()) {
            AlertUtils.showError(owner, "Login Error", "Please enter both username and password.");
            return;
        }

        // Authenticate user against database
        User user = userDAO.authenticate(username, password);

        // Support legacy hardcoded admin user during transition
        boolean isLegacyAdmin = username.equals("admin") && password.equals("admin");

        if (user != null || isLegacyAdmin) {
            try {
                // If using legacy admin, create a User object for session
                if (isLegacyAdmin && user == null) {
                    user = new User();
                    user.setId(0);
                    user.setUsername("admin");
                    user.setPassword("admin");
                }

                // Store user in session manager
                SessionManager.getInstance().setCurrentUser(user);

                // Load the dashboard scene
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/fxml/dashboard.fxml"));
                Parent dashboardParent = loader.load();

                // Get the current stage
                Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();

                // Create scene
                Scene dashboardScene = new Scene(dashboardParent);

                // Set the scene
                stage.setScene(dashboardScene);
                stage.centerOnScreen();
                stage.show();

            } catch (IOException e) {
                AlertUtils.showError(owner, "Navigation Error", "Error loading dashboard: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            AlertUtils.showError(owner, "Login Error", "Invalid username or password.");
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
            // Load the signup screen
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/signup.fxml"));
            Parent signupParent = loader.load();

            // Get the current stage
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();

            // Create scene
            Scene signupScene = new Scene(signupParent);

            // Ensure CSS is applied
            signupScene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/css/login.css")).toExternalForm()
            );

            // Set the scene
            stage.setScene(signupScene);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            Window owner = signUpLink.getScene().getWindow();
            AlertUtils.showError(owner, "Navigation Error", "Error loading signup page: " + e.getMessage());
            e.printStackTrace();
        }
    }
}