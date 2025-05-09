package com.aircraft.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Utility class for displaying JavaFX alerts and dialogs.
 * Provides helper methods for showing information, error, warning, and confirmation dialogs.
 */
public class AlertUtils {

    /**
     * Shows an information alert with the given message.
     *
     * @param owner The owner window for the alert dialog
     * @param title The title of the alert dialog
     * @param message The message to display in the alert dialog
     */
    public static void showInformation(Window owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        configureAlert(alert, owner, title, message);
        alert.showAndWait();
    }

    /**
     * Shows an error alert with the given message.
     *
     * @param owner The owner window for the alert dialog
     * @param title The title of the alert dialog
     * @param message The message to display in the alert dialog
     */
    public static void showError(Window owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        configureAlert(alert, owner, title, message);
        alert.showAndWait();
    }

    /**
     * Shows a warning alert with the given message.
     *
     * @param owner The owner window for the alert dialog
     * @param title The title of the alert dialog
     * @param message The message to display in the alert dialog
     */
    public static void showWarning(Window owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        configureAlert(alert, owner, title, message);
        alert.showAndWait();
    }

    /**
     * Shows a confirmation dialog with the given message.
     *
     * @param owner The owner window for the alert dialog
     * @param title The title of the alert dialog
     * @param message The message to display in the alert dialog
     * @return true if the user confirmed (clicked OK), false otherwise
     */
    public static boolean showConfirmation(Window owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        configureAlert(alert, owner, title, message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Configures an alert dialog with the given parameters.
     *
     * @param alert The Alert object to configure
     * @param owner The owner window for the alert dialog
     * @param title The title of the alert dialog
     * @param message The message to display in the alert dialog
     */
    private static void configureAlert(Alert alert, Window owner, String title, String message) {
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
    }
}