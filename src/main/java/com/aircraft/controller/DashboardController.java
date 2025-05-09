package com.aircraft.controller;

import com.aircraft.util.AlertUtils;
import com.aircraft.util.FXMLUtils;
import com.aircraft.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Window;

import java.io.IOException;

/**
 * Controller for the main dashboard screen.
 * Handles navigation between different modules and displays menu items.
 */
public class DashboardController {
    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private Label usernameLabel;

    @FXML
    private TreeView<String> menuTreeView;

    /**
     * Initializes the controller after its root element has been processed.
     * Sets up event handlers and initializes UI components.
     */
    @FXML
    public void initialize() {
        // Set the current username in the UI
        String username = SessionManager.getInstance().getCurrentUsername();
        usernameLabel.setText(username != null ? username : "Unknown User");

        // Initialize the menu tree
        setupMenuTree();
    }

    /**
     * Sets up the menu tree with all required menu items.
     * Creates a hierarchical tree structure for navigation.
     */
    private void setupMenuTree() {
        // Create root items for the main menu categories
        TreeItem<String> rootItem = new TreeItem<>("Root Menu");

        // Create Data Management menu group
        TreeItem<String> dataManagementItem = new TreeItem<>("Data Management");

        // Create Material Data submenu
        TreeItem<String> materialDataItem = new TreeItem<>("Material Data");
        TreeItem<String> weaponLoadItem = new TreeItem<>("Weapon Load");
        TreeItem<String> launcherItem = new TreeItem<>("Launcher");
        materialDataItem.getChildren().addAll(weaponLoadItem, launcherItem);

        // Create Aircraft Data menu item
        TreeItem<String> aircraftDataItem = new TreeItem<>("Aircraft Data");

        // Create Material Handling menu item
        TreeItem<String> materialHandlingItem = new TreeItem<>("Material Handling");

        // Add all data management items to the Data Management group
        dataManagementItem.getChildren().addAll(materialDataItem, aircraftDataItem, materialHandlingItem);

        // Create Mission Management menu group
        TreeItem<String> missionManagementItem = new TreeItem<>("Mission Management");

        // Create Mission Management subitems
        TreeItem<String> insertMissionItem = new TreeItem<>("Insert New Mission");
        TreeItem<String> missionHistoryItem = new TreeItem<>("Mission History");
        TreeItem<String> pfmdItem = new TreeItem<>("PFMD");

        // Add mission management subitems to the Mission Management group
        missionManagementItem.getChildren().addAll(insertMissionItem, missionHistoryItem, pfmdItem);

        // Create Fatigue Monitoring menu item
        TreeItem<String> fatigueMonitoringItem = new TreeItem<>("Fatigue Monitoring");

        // Add all main menu items to the root
        rootItem.getChildren().addAll(dataManagementItem, missionManagementItem, fatigueMonitoringItem);

        // Set the root item for the TreeView
        menuTreeView.setRoot(rootItem);

        // Hide the root item (we don't want to display "Root Menu")
        menuTreeView.setShowRoot(false);

        // Set up the event handler for tree item selection
        menuTreeView.setOnMouseClicked(this::handleMenuSelection);
    }

    /**
     * Handles menu item selection in the TreeView.
     * Loads the appropriate module based on the selected menu item.
     *
     * @param event The MouseEvent object
     */
    private void handleMenuSelection(MouseEvent event) {
        TreeItem<String> selectedItem = menuTreeView.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            String selectedMenu = selectedItem.getValue();

            try {
                switch (selectedMenu) {
                    case "Weapon Load":
                        loadModule("weapon_load");
                        break;
                    case "Launcher":
                        loadModule("launcher");
                        break;
                    case "Aircraft Data":
                        loadModule("aircraft_data");
                        break;
                    case "Material Handling":
                        loadModule("material_handling");
                        break;
                    case "Insert New Mission":
                        loadModule("mission_management");
                        break;
                    case "Mission History":
                        loadModule("mission_history");
                        break;
                    case "PFMD":
                        loadModule("pfmd");
                        break;
                    case "Fatigue Monitoring":
                        loadModule("fatigue_monitoring");
                        break;
                    default:
                        // Do nothing for parent items or unhandled items
                        break;
                }
            } catch (IOException e) {
                Window owner = menuTreeView.getScene().getWindow();
                AlertUtils.showError(owner, "Navigation Error", "Failed to load module: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads a module into the main content area of the dashboard.
     *
     * @param moduleName The name of the FXML file for the module
     * @throws IOException If the module cannot be loaded
     */
    private void loadModule(String moduleName) throws IOException {
        Pane modulePane = (Pane) FXMLUtils.loadFXML(moduleName);
        mainBorderPane.setCenter(modulePane);
    }

    /**
     * Handles the logout button click event.
     * Clears the current session and navigates back to the login screen.
     *
     * @param event The ActionEvent object
     */
    @FXML
    protected void onLogoutButtonClick(ActionEvent event) {
        // Clear the current session
        SessionManager.getInstance().clearSession();

        try {
            // Navigate back to the login screen
            FXMLUtils.switchScene(mainBorderPane.getScene(), "login");
        } catch (IOException e) {
            Window owner = mainBorderPane.getScene().getWindow();
            AlertUtils.showError(owner, "Navigation Error", "Failed to load login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
}