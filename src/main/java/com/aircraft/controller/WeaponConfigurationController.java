package com.aircraft.controller;

import com.aircraft.dao.LauncherDAO;
import com.aircraft.dao.WeaponDAO;
import com.aircraft.model.Launcher;
import com.aircraft.model.Weapon;
import com.aircraft.util.AlertUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for the weapon configuration screen.
 * Allows selecting weapons and launchers for different positions.
 */
public class WeaponConfigurationController {

    @FXML
    private VBox positionContainer;

    @FXML
    private ComboBox<String> positionComboBox;

    @FXML
    private ComboBox<String> launcherComboBox;

    @FXML
    private ComboBox<String> weaponComboBox;

    @FXML
    private TextField launcherSerialField;

    @FXML
    private TextField weaponSerialField;

    @FXML
    private Button addButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private GridPane positionsGrid;

    private MissionManagementController parentController;
    private final LauncherDAO launcherDAO = new LauncherDAO();
    private final WeaponDAO weaponDAO = new WeaponDAO();

    // Store the selected positions data
    private Map<String, Map<String, String>> selectedPositions = new HashMap<>();

    /**
     * Initializes the controller after its root element has been processed.
     */
    @FXML
    public void initialize() {
        // Set up position combo box with available positions
        setupPositionComboBox();

        // Set up launcher and weapon combo boxes
        setupLauncherComboBox();
        setupWeaponComboBox();

        // Disable weapon selector until launcher is selected
        weaponComboBox.setDisable(true);
        weaponSerialField.setDisable(true);

        // Update UI with any existing position data
        updatePositionsGrid();
    }

    /**
     * Sets up the position combo box with available position codes.
     */
    private void setupPositionComboBox() {
        ObservableList<String> positions = FXCollections.observableArrayList(
                "TIP 1", "O/B 3", "CTR 5", "I/B 7", "FWD 9", "CL 13",
                "FWD 10", "REA 12", "I/B 8", "CTR 6", "O/B 4", "TIP 2"
        );
        positionComboBox.setItems(positions);
    }

    /**
     * Sets up the launcher combo box with launchers from the database.
     */
    private void setupLauncherComboBox() {
        // Load launcher list
        List<Launcher> launcherList = launcherDAO.getAll();
        ObservableList<String> launcherItems = FXCollections.observableArrayList();
        launcherItems.add(""); // Empty option
        launcherItems.addAll(launcherList.stream()
                .map(Launcher::getNomenclatura)
                .collect(Collectors.toList()));
        launcherComboBox.setItems(launcherItems);

        // Set up launcher selection listener
        launcherComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                // Enable weapon selection
                weaponComboBox.setDisable(false);
                weaponSerialField.setDisable(false);
            } else {
                // Disable weapon selection if no launcher is selected
                weaponComboBox.getSelectionModel().clearSelection();
                weaponComboBox.setDisable(true);
                weaponSerialField.setDisable(true);
            }
        });
    }

    /**
     * Sets up the weapon combo box with weapons from the database.
     */
    private void setupWeaponComboBox() {
        // Load weapon list
        List<Weapon> weaponList = weaponDAO.getAll();
        ObservableList<String> weaponItems = FXCollections.observableArrayList();
        weaponItems.add(""); // Empty option
        weaponItems.addAll(weaponList.stream()
                .map(Weapon::getNomenclatura)
                .collect(Collectors.toList()));
        weaponComboBox.setItems(weaponItems);
    }

    /**
     * Sets the parent controller reference.
     *
     * @param controller The parent MissionManagementController
     */
    public void setParentController(MissionManagementController controller) {
        this.parentController = controller;
    }

    /**
     * Sets the selected positions map.
     *
     * @param positions The map of selected positions
     */
    public void setSelectedPositions(Map<String, Map<String, String>> positions) {
        if (positions != null) {
            this.selectedPositions = new HashMap<>(positions);
            updatePositionsGrid();
        }
    }

    /**
     * Updates the positions grid with the current selected positions.
     */
    private void updatePositionsGrid() {
        // Clear existing rows
        positionsGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) > 0);

        int row = 1;

        // Add rows for each selected position
        for (Map.Entry<String, Map<String, String>> entry : selectedPositions.entrySet()) {
            String position = entry.getKey();
            Map<String, String> itemData = entry.getValue();

            // Position label
            Label posLabel = new Label(position);
            positionsGrid.add(posLabel, 0, row);

            // Type label
            Label typeLabel = new Label(itemData.get("type"));
            positionsGrid.add(typeLabel, 1, row);

            // Name label - Look up the name based on ID
            String name = "Unknown";
            if ("launcher".equals(itemData.get("type"))) {
                Optional<Launcher> launcher = launcherDAO.getAll().stream()
                        .filter(l -> l.getPartNumber().equals(itemData.get("id")))
                        .findFirst();

                if (launcher.isPresent()) {
                    name = launcher.get().getNomenclatura();
                }
            } else if ("weapon".equals(itemData.get("type"))) {
                Optional<Weapon> weapon = weaponDAO.getAll().stream()
                        .filter(w -> w.getPartNumber().equals(itemData.get("id")))
                        .findFirst();

                if (weapon.isPresent()) {
                    name = weapon.get().getNomenclatura();
                }
            }
            Label nameLabel = new Label(name);
            positionsGrid.add(nameLabel, 2, row);

            // Serial number label
            Label serialLabel = new Label(itemData.get("serialNumber"));
            positionsGrid.add(serialLabel, 3, row);

            // Remove button
            Button removeBtn = new Button("Remove");
            removeBtn.setOnAction(e -> {
                selectedPositions.remove(position);
                updatePositionsGrid();
            });

            HBox btnContainer = new HBox(removeBtn);
            positionsGrid.add(btnContainer, 4, row);

            row++;
        }
    }

    /**
     * Handles the "Add" button click.
     * Adds the selected launcher or weapon to the position.
     */
    @FXML
    protected void onAddButtonClick(ActionEvent event) {
        Window owner = ((Node) event.getSource()).getScene().getWindow();

        // Validate inputs
        String position = positionComboBox.getValue();
        String launcherName = launcherComboBox.getValue();
        String serial = launcherSerialField.getText();

        if (position == null || position.isEmpty()) {
            AlertUtils.showError(owner, "Validation Error", "Please select a position");
            return;
        }

        if (launcherName == null || launcherName.isEmpty()) {
            AlertUtils.showError(owner, "Validation Error", "Please select a launcher");
            return;
        }

        if (serial == null || serial.isEmpty()) {
            AlertUtils.showError(owner, "Validation Error", "Please enter a serial number");
            return;
        }

        // Get launcher ID
        Optional<Launcher> launcher = launcherDAO.getAll().stream()
                .filter(l -> l.getNomenclatura().equals(launcherName))
                .findFirst();

        if (launcher.isPresent()) {
            // Add launcher to selected positions
            Map<String, String> itemData = new HashMap<>();
            itemData.put("type", "launcher");
            itemData.put("id", launcher.get().getPartNumber());
            itemData.put("serialNumber", serial);

            selectedPositions.put(position, itemData);

            // Add weapon if selected
            String weaponName = weaponComboBox.getValue();
            String weaponSerial = weaponSerialField.getText();

            if (weaponName != null && !weaponName.isEmpty() && weaponSerial != null && !weaponSerial.isEmpty()) {
                Optional<Weapon> weapon = weaponDAO.getAll().stream()
                        .filter(w -> w.getNomenclatura().equals(weaponName))
                        .findFirst();

                if (weapon.isPresent()) {
                    // Add weapon to selected positions
                    Map<String, String> weaponData = new HashMap<>();
                    weaponData.put("type", "weapon");
                    weaponData.put("id", weapon.get().getPartNumber());
                    weaponData.put("serialNumber", weaponSerial);

                    // Use position + "_weapon" as key for weapon
                    selectedPositions.put(position + "_weapon", weaponData);
                }
            }

            // Update UI
            updatePositionsGrid();

            // Clear fields
            positionComboBox.getSelectionModel().clearSelection();
            launcherComboBox.getSelectionModel().clearSelection();
            weaponComboBox.getSelectionModel().clearSelection();
            launcherSerialField.clear();
            weaponSerialField.clear();

        } else {
            AlertUtils.showError(owner, "Error", "Launcher not found");
        }
    }

    /**
     * Handles the "Save" button click.
     * Saves the selected positions and closes the window.
     */
    @FXML
    protected void onSaveButtonClick(ActionEvent event) {
        // Update parent controller
        if (parentController != null) {
            parentController.updateSelectedPositions(selectedPositions);
        }

        // Close window
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Handles the "Cancel" button click.
     * Discards changes and closes the window.
     */
    @FXML
    protected void onCancelButtonClick(ActionEvent event) {
        // Close window without saving
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}