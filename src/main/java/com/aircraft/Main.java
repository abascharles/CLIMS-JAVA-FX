package com.aircraft;

import com.aircraft.util.FXMLUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Main application class that serves as the entry point for the Aircraft Mission Management System.
 * This class initializes the JavaFX application and loads the login screen.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load the login screen with dimensions to accommodate the floating card design
        Scene scene = new Scene(FXMLUtils.loadFXML("login"), 900, 700);

        // Apply CSS styles
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/main.css")).toExternalForm()
        );

        // Add login-specific CSS if not already included in the FXML
        String loginCssPath = "/css/login.css";
        if (getClass().getResource(loginCssPath) != null) {
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource(loginCssPath)).toExternalForm()
            );
        }

        // Configure the primary stage
        primaryStage.setTitle("Aircraft Mission Management System");
        primaryStage.setScene(scene);

        // Set application icon
        primaryStage.getIcons().add(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png")))
        );

        // Make the window non-resizable for consistent UI
        primaryStage.setResizable(false);

        // Center the window on screen
        primaryStage.centerOnScreen();

        // Display the window
        primaryStage.show();
    }

    /**
     * Main method that launches the JavaFX application.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch();
    }
}