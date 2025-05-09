package com.aircraft.util;

import com.aircraft.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Utility class for JavaFX FXML operations.
 * Provides methods for loading FXML files and switching scenes.
 */
public class FXMLUtils {

    /**
     * Loads an FXML file from the resources directory.
     *
     * @param fxml The name of the FXML file without extension
     * @return The root Parent node loaded from the FXML file
     * @throws IOException If the FXML file cannot be loaded
     */
    public static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                Main.class.getResource("/fxml/" + fxml + ".fxml")
        );
        return fxmlLoader.load();
    }

    /**
     * Switches the current scene to a new one loaded from an FXML file.
     *
     * @param currentScene The current Scene object
     * @param fxml The name of the FXML file to load without extension
     * @throws IOException If the FXML file cannot be loaded
     */
    public static void switchScene(Scene currentScene, String fxml) throws IOException {
        Stage stage = (Stage) currentScene.getWindow();
        Scene scene = new Scene(loadFXML(fxml));

        // Apply CSS styles
        scene.getStylesheets().add(
                Objects.requireNonNull(Main.class.getResource("/css/main.css")).toExternalForm()
        );

        // Set the new scene
        stage.setScene(scene);
        stage.show();
    }
}