package com.aircraft.controller;

import com.aircraft.dao.AircraftDAO;
import com.aircraft.dao.LauncherDAO;
import com.aircraft.dao.MissionDAO;
import com.aircraft.dao.WeaponDAO;
import com.aircraft.model.Aircraft;
import com.aircraft.model.Launcher;
import com.aircraft.model.Mission;
import com.aircraft.model.Weapon;
import com.aircraft.util.AlertUtils;
import com.aircraft.util.DBUtil;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Window;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the Mission Management screen with integrated missile position visualization.
 */
public class MissionManagementController {

    @FXML
    private ComboBox<Aircraft> aircraftComboBox;

    @FXML
    private DatePicker missionDatePicker;

    @FXML
    private TextField flightNumberField;

    @FXML
    private TextField timeStartField;

    @FXML
    private TextField timeFinishField;

    @FXML
    private StackPane aircraftContainer;

    @FXML
    private WebView aircraftSvgView;

    @FXML
    private AnchorPane missilePointsContainer;

    @FXML
    private GridPane weaponSelectionPanel;

    @FXML
    private Label selectedPositionLabel;

    @FXML
    private Label validationMessageLabel;

    @FXML
    private ComboBox<String> launcherComboBox;

    @FXML
    private ComboBox<String> weaponComboBox;

    @FXML
    private Button savePositionButton;

    @FXML
    private Button saveAllButton;

    @FXML
    private Button clearAllButton;

    private final Map<String, Pane> missilePointsMap = new HashMap<>();
    private final Map<String, MissionWeaponConfig> missilePositionsData = new HashMap<>();
    private String currentSelectedPosition = null;

    private final AircraftDAO aircraftDAO = new AircraftDAO();
    private final MissionDAO missionDAO = new MissionDAO();
    private final LauncherDAO launcherDAO = new LauncherDAO();
    private final WeaponDAO weaponDAO = new WeaponDAO();

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public void updateSelectedPositions(Map<String, Map<String, String>> selectedPositions) {
    }

    // Class to hold the data for each missile position
    private static class MissionWeaponConfig {
        private final String position;
        private String launcherId;
        private String weaponId;

        public MissionWeaponConfig(String position) {
            this.position = position;
            this.launcherId = null;
            this.weaponId = null;
        }

        public String getPosition() {
            return position;
        }

        public String getLauncherId() {
            return launcherId;
        }

        public void setLauncherId(String launcherId) {
            this.launcherId = launcherId;
        }

        public String getWeaponId() {
            return weaponId;
        }

        public void setWeaponId(String weaponId) {
            this.weaponId = weaponId;
        }

        public boolean hasLauncher() {
            return launcherId != null && !launcherId.isEmpty();
        }

        public boolean hasWeapon() {
            return weaponId != null && !weaponId.isEmpty();
        }

        public boolean isLoaded() {
            return hasLauncher() && hasWeapon();
        }
    }

    /**
     * Initializes the controller after its root element has been processed.
     */
    @FXML
    public void initialize() {
        setupTimeFields();
        setupComboBoxes();
        setupDatePicker();

        // Load SVG after the WebView is fully initialized
        Platform.runLater(this::loadAircraftSvg);
    }

    /**
     * Sets up the time fields with input validation.
     */
    private void setupTimeFields() {
        // Time pattern validation setup
        timeStartField.setPromptText("HH:MM");
        timeFinishField.setPromptText("HH:MM");

        // Add validation to time fields
        timeStartField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // when focus lost
                validateTimeField(timeStartField);
            }
        });

        timeFinishField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // when focus lost
                validateTimeField(timeFinishField);
            }
        });
    }

    /**
     * Validates a time field to ensure it follows the HH:MM format.
     */
    private boolean validateTimeField(TextField field) {
        String timeValue = field.getText().trim();
        if (timeValue.isEmpty()) {
            return true; // Empty is allowed
        }

        try {
            LocalTime.parse(timeValue, timeFormatter);
            field.setStyle(""); // Reset style if valid
            return true;
        } catch (DateTimeParseException e) {
            field.setStyle("-fx-border-color: red;");
            return false;
        }
    }

    /**
     * Sets up the combo boxes with appropriate data.
     */
    private void setupComboBoxes() {
        // Load aircraft list
        List<Aircraft> aircraftList = aircraftDAO.getAll();
        ObservableList<Aircraft> aircraftItems = FXCollections.observableArrayList(aircraftList);
        aircraftComboBox.setItems(aircraftItems);

        // Set up cell factory for aircraft display
        aircraftComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Aircraft item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getMatricolaVelivolo());
                }
            }
        });

        // Set up button cell to display selected aircraft
        aircraftComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Aircraft item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getMatricolaVelivolo());
                }
            }
        });

        // Initially hide weapon selection panel
        weaponSelectionPanel.setVisible(false);

        // Initially hide validation message
        validationMessageLabel.setVisible(false);
    }

    /**
     * Sets up the date picker with default value.
     */
    private void setupDatePicker() {
        missionDatePicker.setValue(LocalDate.now());
    }

    /**
     * Loads the aircraft SVG into the WebView.
     */
    private void loadAircraftSvg() {
        try {
            // Load SVG content from resources
            InputStream svgStream = getClass().getResourceAsStream("/images/aircraft_rear.svg");
            if (svgStream == null) {
                AlertUtils.showError(null, "Error", "Could not load aircraft SVG");
                return;
            }

            String svgContent = new String(svgStream.readAllBytes(), StandardCharsets.UTF_8);

            // Load SVG into WebView
            WebEngine engine = aircraftSvgView.getEngine();
            engine.loadContent("<html><body style='margin:0;overflow:hidden;'>" + svgContent + "</body></html>", "text/html");

            // After SVG is loaded, create missile points
            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    createMissilePoints();
                    setupPositionSelectionListeners();
                }
            });
        } catch (Exception e) {
            AlertUtils.showError(null, "Error", "Failed to load aircraft SVG: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates missile point indicators at the predefined positions.
     */
    private void createMissilePoints() {
        // Define positions - adjusted to align better with the SVG
        // Move all X values slightly to the right (+30px)
        Map<String, double[]> positions = new HashMap<>();
        positions.put("P1", new double[]{90, 170}); // Was 130
        positions.put("P2", new double[]{130, 170}); // Was 160
        positions.put("P3", new double[]{160, 170}); // Was 190
        positions.put("P4", new double[]{190, 170}); // Was 220
        positions.put("P5", new double[]{220, 170}); // Was 250
        positions.put("P6", new double[]{250, 170}); // Was 280
        positions.put("P7", new double[]{280, 170}); // Was 310
        positions.put("P8", new double[]{310, 170}); // Was 340
        positions.put("P9", new double[]{340, 170}); // Was 370
        positions.put("P10", new double[]{370, 170}); // Was 400
        positions.put("P11", new double[]{400, 170}); // Was 430
        positions.put("P12", new double[]{430, 170}); // Was 460
        positions.put("P13", new double[]{460, 170}); // Was 490

        // Create missile position data objects
        for (String position : positions.keySet()) {
            missilePositionsData.put(position, new MissionWeaponConfig(position));
        }

        // Clear any existing missile points
        missilePointsContainer.getChildren().clear();

        // Create visual indicators for each position
        for (Map.Entry<String, double[]> entry : positions.entrySet()) {
            String position = entry.getKey();
            double[] coords = entry.getValue();

            // Create a missile point indicator (rectangle)
            Rectangle point = new Rectangle(24, 40);
            point.getStyleClass().add("missile-position");
            point.getStyleClass().add("missile-empty");

            // Create position label
            Text label = new Text(position);
            label.getStyleClass().add("missile-point-label");

            // Combine in a StackPane
            StackPane missilePoint = new StackPane(point, label);
            missilePoint.setLayoutX(coords[0] - 12); // Center the rectangle
            missilePoint.setLayoutY(coords[1] - 20);

            // Store for later reference
            missilePointsMap.put(position, missilePoint);

            // Add click event
            missilePoint.setOnMouseClicked(event -> selectPosition(position));

            // Add to container
            missilePointsContainer.getChildren().add(missilePoint);
        }
    }

    /**
     * Sets up listeners for position selection changes.
     */
    private void setupPositionSelectionListeners() {
        // Load launcher and weapon lists when a position is selected
        weaponSelectionPanel.visibleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                loadLauncherAndWeaponLists();
            }
        });
    }

    /**
     * Loads the launcher and weapon lists for the position selection panel.
     */
    private void loadLauncherAndWeaponLists() {
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
                // Find the selected launcher
                Optional<Launcher> selectedLauncher = launcherList.stream()
                        .filter(l -> l.getNomenclatura().equals(newVal))
                        .findFirst();

                // Update part number field
                selectedLauncher.ifPresent(launcher -> {
                    if (currentSelectedPosition != null) {
                        MissionWeaponConfig config = missilePositionsData.get(currentSelectedPosition);
                        config.setLauncherId(launcher.getPartNumber());
                    }

                    // Enable weapon selection only after launcher is selected
                    weaponComboBox.setDisable(false);
                    validationMessageLabel.setVisible(false); // Hide error message
                });
            } else {
                // Disable and clear weapon selection if no launcher is selected
                weaponComboBox.getSelectionModel().clearSelection();
                weaponComboBox.setDisable(true);

                if (currentSelectedPosition != null) {
                    MissionWeaponConfig config = missilePositionsData.get(currentSelectedPosition);
                    config.setLauncherId(null);
                    config.setWeaponId(null);
                }
            }
        });

        // Load weapon list
        List<Weapon> weaponList = weaponDAO.getAll();
        ObservableList<String> weaponItems = FXCollections.observableArrayList();
        weaponItems.add(""); // Empty option
        weaponItems.addAll(weaponList.stream()
                .map(Weapon::getNomenclatura)
                .collect(Collectors.toList()));
        weaponComboBox.setItems(weaponItems);
        weaponComboBox.setDisable(true); // Initially disabled until launcher is selected

        // Set up weapon selection listener
        weaponComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                // Check if launcher is selected
                MissionWeaponConfig config = missilePositionsData.get(currentSelectedPosition);
                if (config != null && !config.hasLauncher()) {
                    // Show validation message
                    validationMessageLabel.setText("You must select a launcher before selecting a weapon");
                    validationMessageLabel.setVisible(true);
                    // Reset weapon selection
                    weaponComboBox.setValue("");
                    return;
                }

                // Find the selected weapon
                Optional<Weapon> selectedWeapon = weaponList.stream()
                        .filter(w -> w.getNomenclatura().equals(newVal))
                        .findFirst();

                // Update part number field
                selectedWeapon.ifPresent(weapon -> {
                    if (currentSelectedPosition != null) {
                        config.setWeaponId(weapon.getPartNumber());
                    }
                });

                // Hide validation message if visible
                validationMessageLabel.setVisible(false);
            } else {
                if (currentSelectedPosition != null) {
                    MissionWeaponConfig config = missilePositionsData.get(currentSelectedPosition);
                    config.setWeaponId(null);
                }

                // Hide validation message
                validationMessageLabel.setVisible(false);
            }
        });
    }

    /**
     * Selects a missile position and shows its details.
     *
     * @param position The position identifier (e.g., "P1")
     */
    private void selectPosition(String position) {
        // Clear previous selection
        if (currentSelectedPosition != null && missilePointsMap.containsKey(currentSelectedPosition)) {
            Pane previousPoint = missilePointsMap.get(currentSelectedPosition);
            previousPoint.getStyleClass().remove("missile-selected");

            // Restore original style based on configuration
            updateMissilePointUI(currentSelectedPosition);
        }

        // Set new selection
        currentSelectedPosition = position;

        // Update UI
        Pane selectedPoint = missilePointsMap.get(position);
        selectedPoint.getStyleClass().add("missile-selected");

        // Update position panel
        selectedPositionLabel.setText("Position: " + position);

        // Load position data
        MissionWeaponConfig config = missilePositionsData.get(position);

        // Set launcher and weapon selections based on loaded data
        if (config.hasLauncher()) {
            // Find launcher by part number
            Optional<Launcher> launcher = launcherDAO.getAll().stream()
                    .filter(l -> l.getPartNumber().equals(config.getLauncherId()))
                    .findFirst();

            launcher.ifPresent(l -> launcherComboBox.setValue(l.getNomenclatura()));
        } else {
            launcherComboBox.setValue("");
        }

        if (config.hasWeapon()) {
            // Find weapon by part number
            Optional<Weapon> weapon = weaponDAO.getAll().stream()
                    .filter(w -> w.getPartNumber().equals(config.getWeaponId()))
                    .findFirst();

            weapon.ifPresent(w -> weaponComboBox.setValue(w.getNomenclatura()));
        } else {
            weaponComboBox.setValue("");
        }

        // Reset validation message
        validationMessageLabel.setVisible(false);

        // Show position panel
        weaponSelectionPanel.setVisible(true);
    }

    /**
     * Handles the "Save Position" button click.
     */
    @FXML
    protected void onSavePositionClick(ActionEvent event) {
        if (currentSelectedPosition == null) return;

        Window owner = ((Node) event.getSource()).getScene().getWindow();
        MissionWeaponConfig config = missilePositionsData.get(currentSelectedPosition);

        // Validate launcher selection
        if (!config.hasLauncher()) {
            validationMessageLabel.setText("Launcher is required");
            validationMessageLabel.setVisible(true);
            return;
        }

        // Update UI to reflect saved state
        updateMissilePointUI(currentSelectedPosition);

        // Hide validation message
        validationMessageLabel.setVisible(false);

        AlertUtils.showInformation(owner, "Success",
                "Position " + currentSelectedPosition + " configuration saved");
    }

    /**
     * Updates the UI for a specific missile position.
     */
    private void updateMissilePointUI(String position) {
        MissionWeaponConfig config = missilePositionsData.get(position);
        Pane missilePoint = missilePointsMap.get(position);

        // Reset styles
        if (missilePoint != null) {
            Rectangle rect = (Rectangle) ((StackPane) missilePoint).getChildren().get(0);

            // Clear existing state classes
            rect.getStyleClass().removeAll("missile-empty", "missile-onboard", "missile-fired");

            // If the position has a launcher and weapon, show it as loaded
            if (config.isLoaded()) {
                rect.getStyleClass().add("missile-onboard");
            } else {
                // Reset to default
                rect.getStyleClass().add("missile-empty");
            }
        }
    }

    /**
     * Handles the "Save All" button click.
     */
    @FXML
    protected void onSaveAllClick(ActionEvent event) {
        Window owner = ((Node) event.getSource()).getScene().getWindow();

        // Validate input fields
        Aircraft selectedAircraft = aircraftComboBox.getValue();
        LocalDate missionDate = missionDatePicker.getValue();
        String flightNumber = flightNumberField.getText();
        String timeStart = timeStartField.getText();
        String timeFinish = timeFinishField.getText();

        if (selectedAircraft == null ||
                missionDate == null ||
                flightNumber == null || flightNumber.isEmpty()) {
            AlertUtils.showError(owner, "Validation Error",
                    "Aircraft, date, and flight number are required");
            return;
        }

        // Validate time fields format if they are not empty
        boolean timeStartValid = timeStart.isEmpty() || validateTimeField(timeStartField);
        boolean timeFinishValid = timeFinish.isEmpty() || validateTimeField(timeFinishField);

        if (!timeStartValid || !timeFinishValid) {
            AlertUtils.showError(owner, "Validation Error", "Please enter valid time values in HH:MM format");
            return;
        }

        // Count loaded positions
        long loadedPositions = missilePositionsData.values().stream()
                .filter(MissionWeaponConfig::isLoaded)
                .count();

        // Create a new mission
        Mission mission = new Mission();
        mission.setMatricolaVelivolo(selectedAircraft.getMatricolaVelivolo());
        try {
            mission.setNumeroVolo(Integer.parseInt(flightNumber));
        } catch (NumberFormatException e) {
            AlertUtils.showError(owner, "Validation Error", "Flight number must be a valid integer");
            return;
        }
        mission.setDataMissione(java.sql.Date.valueOf(missionDate));

        // Convert time strings to SQL Time objects if not empty
        if (!timeStart.isEmpty()) {
            try {
                LocalTime localTimeStart = LocalTime.parse(timeStart, timeFormatter);
                mission.setOraPartenza(Time.valueOf(localTimeStart));
            } catch (Exception e) {
                AlertUtils.showError(owner, "Error", "Invalid time format: " + e.getMessage());
                return;
            }
        }

        if (!timeFinish.isEmpty()) {
            try {
                LocalTime localTimeFinish = LocalTime.parse(timeFinish, timeFormatter);
                mission.setOraArrivo(Time.valueOf(localTimeFinish));
            } catch (Exception e) {
                AlertUtils.showError(owner, "Error", "Invalid time format: " + e.getMessage());
                return;
            }
        }

        // Save the mission
        boolean success = missionDAO.insert(mission);

        if (success) {
            // Get the mission ID
            List<Mission> latestMissions = missionDAO.getLatestMissions(1);
            if (!latestMissions.isEmpty()) {
                mission = latestMissions.get(0);

                // In a real app, this would now save all the weapon configurations to the database
                String message = "Mission saved successfully";
                if (loadedPositions > 0) {
                    message += " with " + loadedPositions + " configured positions";
                }

                AlertUtils.showInformation(owner, "Success", message);

                // Clear form and reset UI
                clearForm();
            } else {
                AlertUtils.showError(owner, "Error", "Failed to get newly created mission");
            }
        } else {
            AlertUtils.showError(owner, "Error", "Failed to save mission");
        }
    }

    /**
     * Handles the "Clear All" button click.
     */
    @FXML
    protected void onClearAllClick(ActionEvent event) {
        clearForm();
    }

    /**
     * Clears all form fields and resets missile positions.
     */
    private void clearForm() {
        aircraftComboBox.setValue(null);
        flightNumberField.clear();
        missionDatePicker.setValue(LocalDate.now());
        timeStartField.clear();
        timeFinishField.clear();

        // Reset all missile positions
        for (String position : missilePositionsData.keySet()) {
            MissionWeaponConfig config = missilePositionsData.get(position);
            config.setLauncherId(null);
            config.setWeaponId(null);
        }

        // Hide weapon selection panel
        weaponSelectionPanel.setVisible(false);
        currentSelectedPosition = null;

        // Update UI
        for (Map.Entry<String, Pane> entry : missilePointsMap.entrySet()) {
            Rectangle rect = (Rectangle) ((StackPane) entry.getValue()).getChildren().get(0);
            rect.getStyleClass().removeAll("missile-onboard", "missile-fired", "missile-selected");
            rect.getStyleClass().add("missile-empty");
        }

        // Clear weapon selection
        launcherComboBox.setValue("");
        weaponComboBox.setValue("");

        // Hide validation message
        validationMessageLabel.setVisible(false);
    }
}