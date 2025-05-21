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
import java.sql.*;
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
        private String launcherSerialNumber;

        public MissionWeaponConfig(String position) {
            this.position = position;
            this.launcherId = null;
            this.weaponId = null;
            this.launcherSerialNumber = null;
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

        public String getLauncherSerialNumber() {
            return launcherSerialNumber;
        }

        public void setLauncherSerialNumber(String launcherSerialNumber) {
            this.launcherSerialNumber = launcherSerialNumber;
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
        // Define all positions but only show P1 and P13
        Map<String, double[]> positions = new HashMap<>();
        positions.put("P1", new double[]{90, 170});
        positions.put("P2", new double[]{130, 170});
        positions.put("P3", new double[]{160, 170});
        positions.put("P4", new double[]{190, 170});
        positions.put("P5", new double[]{220, 170});
        positions.put("P6", new double[]{250, 170});
        positions.put("P7", new double[]{280, 170});
        positions.put("P8", new double[]{310, 170});
        positions.put("P9", new double[]{340, 170});
        positions.put("P10", new double[]{370, 170});
        positions.put("P11", new double[]{400, 170});
        positions.put("P12", new double[]{430, 170});
        positions.put("P13", new double[]{460, 170});

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

            // Only positions P1 and P13 are configurable, hide others
            if (position.equals("P1") || position.equals("P13")) {
                point.setFill(Color.TRANSPARENT);
                point.setStroke(Color.GREEN); // Highlight configurable positions
                point.setStrokeWidth(2.0);

                // Create position label
                Text label = new Text(position);
                label.getStyleClass().add("missile-point-label");
                label.setFill(Color.BLACK);

                // Combine in a StackPane
                StackPane missilePoint = new StackPane(point, label);
                missilePoint.setLayoutX(coords[0] - 12);
                missilePoint.setLayoutY(coords[1] - 20);

                // Add click event only to configurable positions
                missilePoint.setOnMouseClicked(event -> selectPosition(position));

                // Store for later reference
                missilePointsMap.put(position, missilePoint);

                // Add to container
                missilePointsContainer.getChildren().add(missilePoint);
            }
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
            } else {
                // When the panel is hidden, update the position to its true state
                updatePositionStatus();
            }
        });

        // Add a listener for clicking on the background
        missilePointsContainer.setOnMouseClicked(event -> {
            if (event.getTarget() == missilePointsContainer) {
                // Only if clicked directly on the container, not on a position
                updatePositionStatus();
                currentSelectedPosition = null;
                weaponSelectionPanel.setVisible(false);
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

        // Show both nomenclature and part number in the dropdown
        launcherItems.addAll(launcherList.stream()
                .map(l -> l.getNomenclatura() + " (" + l.getPartNumber() + ")")
                .collect(Collectors.toList()));
        launcherComboBox.setItems(launcherItems);

        // Set up launcher selection listener
        launcherComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                // Extract part number from string like "Nomenclature (PartNumber)"
                String partNumberWithParens = newVal.substring(newVal.lastIndexOf("("));
                String partNumber = partNumberWithParens.substring(1, partNumberWithParens.length() - 1);

                // Find the selected launcher
                Optional<Launcher> selectedLauncher = launcherList.stream()
                        .filter(l -> l.getPartNumber().equals(partNumber))
                        .findFirst();

                // Update part number field
                selectedLauncher.ifPresent(launcher -> {
                    if (currentSelectedPosition != null) {
                        MissionWeaponConfig config = missilePositionsData.get(currentSelectedPosition);
                        config.setLauncherId(launcher.getPartNumber());

                        // Generate a serial number from part number
                        String serialPrefix = launcher.getPartNumber().length() >= 4 ?
                                launcher.getPartNumber().substring(0, 4) :
                                launcher.getPartNumber();
                        config.setLauncherSerialNumber("SN" + serialPrefix);
                    }

                    // Enable weapon selection only after launcher is selected
                    weaponComboBox.setDisable(false);
                    validationMessageLabel.setVisible(false);
                });
            } else {
                // Existing code for handling null selection
                weaponComboBox.getSelectionModel().clearSelection();
                weaponComboBox.setDisable(true);

                if (currentSelectedPosition != null) {
                    MissionWeaponConfig config = missilePositionsData.get(currentSelectedPosition);
                    config.setLauncherId(null);
                    config.setWeaponId(null);
                    config.setLauncherSerialNumber(null);
                }
            }
        });

        // Update the weapons dropdown similarly
        List<Weapon> weaponList = weaponDAO.getAll();
        ObservableList<String> weaponItems = FXCollections.observableArrayList();
        weaponItems.add(""); // Empty option

        // Show both nomenclature and part number in the dropdown
        weaponItems.addAll(weaponList.stream()
                .map(w -> w.getNomenclatura() + " (" + w.getPartNumber() + ")")
                .collect(Collectors.toList()));
        weaponComboBox.setItems(weaponItems);
        weaponComboBox.setDisable(true); // Initially disabled

        // Set up weapon selection listener
        weaponComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                // Check if launcher is selected
                MissionWeaponConfig config = missilePositionsData.get(currentSelectedPosition);
                if (config != null && !config.hasLauncher()) {
                    validationMessageLabel.setText("You must select a launcher before selecting a weapon");
                    validationMessageLabel.setVisible(true);
                    weaponComboBox.setValue("");
                    return;
                }

                // Extract part number from string
                String partNumberWithParens = newVal.substring(newVal.lastIndexOf("("));
                String partNumber = partNumberWithParens.substring(1, partNumberWithParens.length() - 1);

                // Find the selected weapon
                Optional<Weapon> selectedWeapon = weaponList.stream()
                        .filter(w -> w.getPartNumber().equals(partNumber))
                        .findFirst();

                // Update part number field
                selectedWeapon.ifPresent(weapon -> {
                    if (currentSelectedPosition != null) {
                        config.setWeaponId(weapon.getPartNumber());
                    }
                });

                validationMessageLabel.setVisible(false);
            } else {
                if (currentSelectedPosition != null) {
                    MissionWeaponConfig config = missilePositionsData.get(currentSelectedPosition);
                    config.setWeaponId(null);
                }
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

        // Only positions P1 and P13 are configurable
        if (!position.equals("P1") && !position.equals("P13")) {
            validationMessageLabel.setText("Only positions P1 and P13 are configurable");
            validationMessageLabel.setVisible(true);
            return;
        }
        // Update previous position based on its actual configuration
        if (currentSelectedPosition != null && missilePointsMap.containsKey(currentSelectedPosition)) {
            // First update based on configuration
            updateMissilePointUI(currentSelectedPosition);

            // Then reset styling
            Pane previousPoint = missilePointsMap.get(currentSelectedPosition);
            Rectangle rect = (Rectangle) ((StackPane) previousPoint).getChildren().get(0);
            rect.setStroke(Color.GRAY); // Reset border color
            rect.setStrokeWidth(1.5);
        }

        // Set new selection
        currentSelectedPosition = position;

        // Update UI - highlight with green border
        Pane selectedPoint = missilePointsMap.get(position);
        Rectangle rect = (Rectangle) ((StackPane) selectedPoint).getChildren().get(0);
        rect.setStroke(Color.GREEN); // Highlight with green border
        rect.setStrokeWidth(2.5); // Make border thicker

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

            launcher.ifPresent(l -> launcherComboBox.setValue(l.getNomenclatura() + " (" + l.getPartNumber() + ")"));
        } else {
            launcherComboBox.setValue("");
        }

        if (config.hasWeapon()) {
            // Find weapon by part number
            Optional<Weapon> weapon = weaponDAO.getAll().stream()
                    .filter(w -> w.getPartNumber().equals(config.getWeaponId()))
                    .findFirst();

            weapon.ifPresent(w -> weaponComboBox.setValue(w.getNomenclatura() + " (" + w.getPartNumber() + ")"));
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
     * This will save both the position configuration AND the mission (if not already saved).
     * Uses a temporary fix to avoid position mapping issues.
     */
    private Integer currentMissionId = null;

    /**
     * Handles the "Save Position" button click.
     * This will save both the position configuration AND the mission (if not already saved).
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

        // We only care about positions 1 and 13 (the extremes)
        if (!currentSelectedPosition.equals("P1") && !currentSelectedPosition.equals("P13")) {
            AlertUtils.showError(owner, "Invalid Position",
                    "Only positions P1 and P13 are configurable in this version.");
            return;
        }

        // First, save the mission if it hasn't been saved yet
        if (currentMissionId == null || currentMissionId <= 0) {
            // Mission validation
            Aircraft selectedAircraft = aircraftComboBox.getValue();
            LocalDate missionDate = missionDatePicker.getValue();
            String flightNumber = flightNumberField.getText();
            String timeStart = timeStartField.getText();
            String timeFinish = timeFinishField.getText();

            if (selectedAircraft == null || missionDate == null ||
                    flightNumber == null || flightNumber.isEmpty()) {
                validationMessageLabel.setText("Aircraft, date, and flight number are required");
                validationMessageLabel.setVisible(true);
                return;
            }

            // Validate time fields format if not empty
            boolean timeStartValid = timeStart.isEmpty() || validateTimeField(timeStartField);
            boolean timeFinishValid = timeFinish.isEmpty() || validateTimeField(timeFinishField);

            if (!timeStartValid || !timeFinishValid) {
                validationMessageLabel.setText("Please enter valid time values in HH:MM format");
                validationMessageLabel.setVisible(true);
                return;
            }

            // Parse flight number
            int flightNum;
            try {
                flightNum = Integer.parseInt(flightNumber);
            } catch (NumberFormatException e) {
                validationMessageLabel.setText("Flight number must be a valid integer");
                validationMessageLabel.setVisible(true);
                return;
            }

            // Check if this flight number already exists for this aircraft
            if (missionDAO.flightNumberExists(selectedAircraft.getMatricolaVelivolo(), flightNum)) {
                AlertUtils.showError(owner, "Validation Error",
                        "Flight number " + flightNum + " already exists for aircraft " + selectedAircraft.getMatricolaVelivolo());
                validationMessageLabel.setText("Flight number " + flightNum + " already exists for aircraft " + selectedAircraft.getMatricolaVelivolo());
                validationMessageLabel.setVisible(true);
                return;
            }

            // Create mission object with the new fields
            Mission mission = new Mission();
            mission.setMatricolaVelivolo(selectedAircraft.getMatricolaVelivolo());
            mission.setNumeroVolo(flightNum);
            mission.setDataMissione(java.sql.Date.valueOf(missionDate));

            // Convert time strings to SQL Time objects if not empty
            if (!timeStart.isEmpty()) {
                try {
                    LocalTime localTimeStart = LocalTime.parse(timeStart, timeFormatter);
                    mission.setOraPartenza(Time.valueOf(localTimeStart));
                } catch (Exception e) {
                    validationMessageLabel.setText("Invalid time format");
                    validationMessageLabel.setVisible(true);
                    return;
                }
            }

            if (!timeFinish.isEmpty()) {
                try {
                    LocalTime localTimeFinish = LocalTime.parse(timeFinish, timeFormatter);
                    mission.setOraArrivo(Time.valueOf(localTimeFinish));
                } catch (Exception e) {
                    validationMessageLabel.setText("Invalid time format");
                    validationMessageLabel.setVisible(true);
                    return;
                }
            }

            // Set the launcher and missile values for positions 1 and 13
            if (currentSelectedPosition.equals("P1")) {
                mission.setLauncherPN1(config.getLauncherId());
                mission.setMissilePN1(config.getWeaponId());
            } else if (currentSelectedPosition.equals("P13")) {
                mission.setLauncherPN13(config.getLauncherId());
                mission.setMissilePN13(config.getWeaponId());
            }

            // Save mission and get ID
            try {
                currentMissionId = missionDAO.insertAndGetId(mission);
                if (currentMissionId <= 0) {
                    validationMessageLabel.setText("Failed to save mission");
                    validationMessageLabel.setVisible(true);
                    return;
                }
            } catch (Exception e) {
                validationMessageLabel.setText("Error saving mission: " + e.getMessage());
                validationMessageLabel.setVisible(true);
                e.printStackTrace();
                return;
            }
        } else {
            // Update existing mission with the selected position data
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = DBUtil.getConnection();

                // Determine which fields to update based on position
                String updateField1, updateField2;
                Object value1, value2;

                if (currentSelectedPosition.equals("P1")) {
                    updateField1 = "PartNumberLanciatoreP1";
                    updateField2 = "PartNumberMissileP1";
                    value1 = config.getLauncherId();
                    value2 = config.getWeaponId();
                } else if (currentSelectedPosition.equals("P13")) {
                    updateField1 = "PartNumberLanciatoreP13";
                    updateField2 = "PartNumberMissileP13";
                    value1 = config.getLauncherId();
                    value2 = config.getWeaponId();
                } else {
                    throw new IllegalArgumentException("Only positions P1 and P13 are configurable");
                }

                // Update the mission record
                String sql = "UPDATE missione SET " + updateField1 + " = ?, " + updateField2 + " = ? WHERE ID = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, (String)value1);
                stmt.setString(2, (String)value2);
                stmt.setInt(3, currentMissionId);

                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new SQLException("Failed to update mission, no rows affected");
                }
            } catch (SQLException e) {
                validationMessageLabel.setText("Error updating mission: " + e.getMessage());
                validationMessageLabel.setVisible(true);
                e.printStackTrace();
                return;
            } finally {
                DBUtil.closeResources(conn, stmt, null);
            }
        }

        // Update UI to reflect saved state
        updateMissilePointUI(currentSelectedPosition);

        // Turn the save button green to indicate successful save
        savePositionButton.setStyle("-fx-background-color: #2e7d32;");

        // Hide validation message
        validationMessageLabel.setVisible(false);

        AlertUtils.showInformation(owner, "Position Saved",
                "Position " + currentSelectedPosition + " configuration saved to database successfully.");
    }

    /**
     * Updates the UI for a specific missile position.
     */
    private void updateMissilePointUI(String position) {
        MissionWeaponConfig config = missilePositionsData.get(position);
        Pane missilePoint = missilePointsMap.get(position);

        if (missilePoint != null) {
            Rectangle rect = (Rectangle) ((StackPane) missilePoint).getChildren().get(0);

            // Clear existing state styles
            rect.setFill(Color.TRANSPARENT);

            // If the position has a launcher and weapon, show it as loaded
            if (config.isLoaded()) {
                rect.setFill(Color.LIGHTGREEN);
                rect.setOpacity(0.7);
            }
        }
    }
    /**
     * Add this new method to the MissionManagementController class
     * to handle when a user navigates away from a position without saving.
     */
    private void updatePositionStatus() {
        // If there was a previously selected position, update its UI based on actual configuration
        if (currentSelectedPosition != null && missilePointsMap.containsKey(currentSelectedPosition)) {
            updateMissilePointUI(currentSelectedPosition);
        }
    }

    /**
     * Handles the "Save All" button click.
     * Saves the mission and its weapon configurations to the database.
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

        // Parse flight number
        int flightNum;
        try {
            flightNum = Integer.parseInt(flightNumber);
        } catch (NumberFormatException e) {
            AlertUtils.showError(owner, "Validation Error", "Flight number must be a valid integer");
            return;
        }

        // Check if this flight number already exists for this aircraft
        if (missionDAO.flightNumberExists(selectedAircraft.getMatricolaVelivolo(), flightNum)) {
            AlertUtils.showError(owner, "Validation Error",
                    "Flight number " + flightNum + " already exists for aircraft " + selectedAircraft.getMatricolaVelivolo());
            return;
        }

        // Count loaded positions
        long loadedPositions = missilePositionsData.values().stream()
                .filter(MissionWeaponConfig::isLoaded)
                .count();

        // Create a new mission with launcher and missile data
        Mission mission = new Mission();
        mission.setMatricolaVelivolo(selectedAircraft.getMatricolaVelivolo());
        mission.setNumeroVolo(flightNum);
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

        // Set launcher and missile part numbers for positions 1 and 13
        MissionWeaponConfig config1 = missilePositionsData.get("P1");
        if (config1 != null && config1.isLoaded()) {
            mission.setLauncherPN1(config1.getLauncherId());
            mission.setMissilePN1(config1.getWeaponId());
        }

        MissionWeaponConfig config13 = missilePositionsData.get("P13");
        if (config13 != null && config13.isLoaded()) {
            mission.setLauncherPN13(config13.getLauncherId());
            mission.setMissilePN13(config13.getWeaponId());
        }

        // Save the mission
        int missionId = missionDAO.insertAndGetId(mission);

        if (missionId > 0) {
            currentMissionId = missionId;

            String message = "Mission saved successfully";
            if (config1 != null && config1.isLoaded() && config13 != null && config13.isLoaded()) {
                message += " with launcher and missile data for positions P1 and P13";
            } else if (config1 != null && config1.isLoaded()) {
                message += " with launcher and missile data for position P1";
            } else if (config13 != null && config13.isLoaded()) {
                message += " with launcher and missile data for position P13";
            }

            AlertUtils.showInformation(owner, "Success", message);

            // Clear form and reset UI
            clearForm();
        } else {
            AlertUtils.showError(owner, "Error", "Failed to save mission");
        }
    }

    /**
     * Maps UI position identifier to database position code.
     * Includes extra debugging to ensure valid positions.
     *
     * @param uiPosition The UI position identifier (P1-P13)
     * @return The corresponding database position code
     */
    private String mapUiPositionToDbPosition(String uiPosition) {
        // Trim whitespace and ensure consistent case
        String position = uiPosition.trim();

        // Log the input position for debugging
        System.out.println("Mapping UI position: '" + position + "'");

        // Direct mapping for all positions P1-P13
        // We're assuming you've added all these positions to the database
        if (position.matches("P\\d+")) {
            System.out.println("Mapped to DB position: '" + position + "'");
            return position;
        }

        // Fall back to existing positions if something unexpected happens
        switch (position) {
            case "TIP 1": return "P1";
            case "O/B 3": return "P2";
            case "CTR 5": return "P3";
            case "I/B 7": return "P4";
            case "FWD 9": return "P5";
            case "CL 13": return "P6";
            case "CL 14": return "P7";
            case "REA 12": return "P8";
            case "FWD 10": return "P9";
            case "I/B 8": return "P10";
            case "CTR 6": return "P11";
            case "O/B 4": return "P12";
            case "TIP 2": return "P13";
            default:
                // If we get here, something unexpected happened
                System.out.println("WARNING: Unknown position: '" + position + "', defaulting to 'P1'");
                return "P1";  // Default to P1 as a fallback
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
            config.setLauncherSerialNumber(null);
        }

        // Hide weapon selection panel
        weaponSelectionPanel.setVisible(false);
        currentSelectedPosition = null;

        // Update UI
        for (Map.Entry<String, Pane> entry : missilePointsMap.entrySet()) {
            StackPane pane = (StackPane) entry.getValue();
            Rectangle rect = (Rectangle) pane.getChildren().get(0);

            rect.setFill(Color.TRANSPARENT);
            rect.setStroke(Color.GRAY);
            rect.setStrokeWidth(1.5);
        }

        // Clear weapon selection
        launcherComboBox.setValue("");
        weaponComboBox.setValue("");

        // Hide validation message
        validationMessageLabel.setVisible(false);
    }
    }
