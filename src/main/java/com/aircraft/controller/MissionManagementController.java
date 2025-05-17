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

            // Create a missile point indicator (rectangle) - TRANSPARENT instead of filled
            Rectangle point = new Rectangle(24, 40);
            point.setFill(Color.TRANSPARENT); // Set to transparent instead of filled
            point.setStroke(Color.GRAY); // Add a border
            point.setStrokeWidth(1.5); // Make border visible

            // Create position label
            Text label = new Text(position);
            label.getStyleClass().add("missile-point-label");
            label.setFill(Color.BLACK); // Ensure label is visible

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
            // For duplicate flight numbers:
            if (missionDAO.flightNumberExists(selectedAircraft.getMatricolaVelivolo(), flightNum)) {
                // Show error popup using AlertUtils
                AlertUtils.showError(owner, "Validation Error",
                        "Flight number " + flightNum + " already exists for aircraft " + selectedAircraft.getMatricolaVelivolo());
                // Also update the validation label for additional visibility
                validationMessageLabel.setText("Flight number " + flightNum + " already exists for aircraft " + selectedAircraft.getMatricolaVelivolo());
                validationMessageLabel.setVisible(true);
                return;
            }

            // Create mission object
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
        }

        // Rest of the method remains unchanged
        // Check if this position is already configured
        boolean positionAlreadyConfigured = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String dbPosition = mapUiPositionToDbPosition(currentSelectedPosition);

            // Check if this position already exists for this mission
            String checkSql = "SELECT 1 FROM stato_missili_missione WHERE ID_Missione = ? AND PosizioneVelivolo = ?";
            stmt = conn.prepareStatement(checkSql);
            stmt.setInt(1, currentMissionId);
            stmt.setString(2, dbPosition);

            rs = stmt.executeQuery();
            positionAlreadyConfigured = rs.next();
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }

        // If position is already configured, ask for confirmation
        if (positionAlreadyConfigured) {
            boolean confirmed = AlertUtils.showConfirmation(
                    owner,
                    "Position Already Configured",
                    "Position " + currentSelectedPosition + " is already configured. Do you want to overwrite it?"
            );

            if (!confirmed) {
                return; // Don't proceed if user doesn't confirm
            }
        }

        // Now save the position configuration
        conn = null;
        stmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);  // Start transaction

            // Map UI position to database position
            String dbPosition = mapUiPositionToDbPosition(currentSelectedPosition);

            System.out.println("Saving position: " + currentSelectedPosition + " -> " + dbPosition + " for mission: " + currentMissionId);

            // Check if mission ID is still valid
            if (currentMissionId == null || currentMissionId <= 0) {
                validationMessageLabel.setText("Invalid mission ID. Please save the mission first.");
                validationMessageLabel.setVisible(true);
                return;
            }

            // Now proceed with position saving
            String checkSql = "SELECT ID FROM stato_missili_missione WHERE ID_Missione = ? AND PosizioneVelivolo = ?";
            stmt = conn.prepareStatement(checkSql);
            stmt.setInt(1, currentMissionId);
            stmt.setString(2, dbPosition);

            // Check if this position already has a configuration for this mission
            rs = stmt.executeQuery();
            boolean exists = rs.next();
            rs.close();
            stmt.close();

            if (exists) {
                // Update existing configuration
                String updateSql = "UPDATE stato_missili_missione SET " +
                        "Lanciatore_PartNumber = ?, Lanciatore_SerialNumber = ?, " +
                        "PartNumber = ?, Nomenclatura = ?, Stato = ? " +
                        "WHERE ID_Missione = ? AND PosizioneVelivolo = ?";

                stmt = conn.prepareStatement(updateSql);
                stmt.setString(1, config.getLauncherId());
                stmt.setString(2, config.getLauncherSerialNumber());
                stmt.setString(3, config.getWeaponId());

                // Get weapon nomenclatura from DB
                Weapon weapon = null;
                if (config.getWeaponId() != null) {
                    weapon = weaponDAO.getByPartNumber(config.getWeaponId());
                }
                stmt.setString(4, weapon != null ? weapon.getNomenclatura() : null);

                // Default status is "A_BORDO" (on board)
                stmt.setString(5, "A_BORDO");
                stmt.setInt(6, currentMissionId);
                stmt.setString(7, dbPosition);

                stmt.executeUpdate();
                stmt.close();
            } else {
                // Insert new configuration
                String insertSql = "INSERT INTO stato_missili_missione (ID_Missione, PosizioneVelivolo, " +
                        "Lanciatore_PartNumber, Lanciatore_SerialNumber, PartNumber, Nomenclatura, Stato) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

                stmt = conn.prepareStatement(insertSql);
                stmt.setInt(1, currentMissionId);
                stmt.setString(2, dbPosition);
                stmt.setString(3, config.getLauncherId());
                stmt.setString(4, config.getLauncherSerialNumber());
                stmt.setString(5, config.getWeaponId());


                // Get weapon nomenclatura from DB
                Weapon weapon = null;
                if (config.getWeaponId() != null) {
                    weapon = weaponDAO.getByPartNumber(config.getWeaponId());
                }
                stmt.setString(6, weapon != null ? weapon.getNomenclatura() : null);

                // Default status is "A_BORDO" (on board)
                stmt.setString(7, "A_BORDO");

                stmt.executeUpdate();
                stmt.close();

                // Add to storico tables
                Mission mission = missionDAO.getById(currentMissionId);

                // Check if already in storico_lanciatore
                String checkLanciatoreSql = "SELECT ID FROM storico_lanciatore WHERE " +
                        "MatricolaVelivolo = ? AND PosizioneVelivolo = ? AND PartNumber = ?";
                stmt = conn.prepareStatement(checkLanciatoreSql);
                stmt.setString(1, mission.getMatricolaVelivolo());
                stmt.setString(2, dbPosition);
                stmt.setString(3, config.getLauncherId());
                rs = stmt.executeQuery();
                boolean lanciatorExists = rs.next();
                rs.close();
                stmt.close();

                if (!lanciatorExists) {
                    // Insert into storico_lanciatore - MODIFIED: removed SerialNumber
                    stmt = conn.prepareStatement(
                            "INSERT INTO storico_lanciatore (MatricolaVelivolo, PartNumber, " +
                                    "DataInstallazione, DataRimozione, PosizioneVelivolo) " +
                                    "VALUES (?, ?, ?, NULL, ?)"
                    );

                    stmt.setString(1, mission.getMatricolaVelivolo());
                    stmt.setString(2, config.getLauncherId());
                    stmt.setDate(3, mission.getDataMissione());
                    stmt.setString(4, dbPosition);

                    stmt.executeUpdate();
                    stmt.close();
                }

                // Check if already in storico_carico
                if (config.getWeaponId() != null) {
                    String checkCaricoSql = "SELECT ID FROM storico_carico WHERE " +
                            "PartNumber = ? AND PosizioneVelivolo = ?";
                    stmt = conn.prepareStatement(checkCaricoSql);
                    stmt.setString(1, config.getWeaponId());
                    stmt.setString(2, dbPosition);
                    rs = stmt.executeQuery();
                    boolean caricoExists = rs.next();
                    rs.close();
                    stmt.close();

                    if (!caricoExists) {
                        // Insert into storico_carico with the correct column structure
                        stmt = conn.prepareStatement(
                                "INSERT INTO storico_carico (PartNumber, Nomenclatura, " +
                                        "DataImbarco, DataSbarco, PosizioneVelivolo, MatricolaVelivolo) " +
                                        "VALUES (?, ?, ?, NULL, ?, ?)"
                        );

                        stmt.setString(1, config.getWeaponId());

                        // Get weapon nomenclatura from DB
                        Weapon weaponForPosition = weaponDAO.getByPartNumber(config.getWeaponId());
                        stmt.setString(2, weaponForPosition != null ? weaponForPosition.getNomenclatura() : null);

                        stmt.setDate(3, mission.getDataMissione());
                        stmt.setString(4, dbPosition);
                        stmt.setString(5, mission.getMatricolaVelivolo());

                        stmt.executeUpdate();
                        stmt.close();
                        System.out.println("Added to storico_carico"); // Debug output
                    }
                }
            }

            // Commit transaction
            conn.commit();
            success = true;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            // Display the full error message for debugging
            AlertUtils.showError(owner, "Database Error",
                    "Error saving position: " + e.getMessage() + "\nSQL State: " + e.getSQLState());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (success) {
            // Update UI to reflect saved state
            updateMissilePointUI(currentSelectedPosition);

            // Turn the save button green to indicate successful save
            savePositionButton.setStyle("-fx-background-color: #2e7d32;");

            // Hide validation message
            validationMessageLabel.setVisible(false);

            AlertUtils.showInformation(owner, "Position Saved",
                    "Position " + currentSelectedPosition + " configuration saved to database successfully.");
        }
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

        // Create a new mission
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

        Connection conn = null;
        boolean success = false;

        try {
            // Get database connection
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Save the mission and get the ID directly - KEY FIX
            int missionId = missionDAO.insertAndGetId(mission);

            if (missionId > 0) {
                // Set the ID in the mission object
                mission.setId(missionId);
                System.out.println("Saved mission with ID: " + missionId); // Debug output

                // Now save the weapon configurations
                for (Map.Entry<String, MissionWeaponConfig> entry : missilePositionsData.entrySet()) {
                    MissionWeaponConfig config = entry.getValue();

                    // Skip if no launcher/weapon configured
                    if (!config.isLoaded()) continue;

                    // Get DB position code (may need mapping)
                    String dbPosition = mapUiPositionToDbPosition(entry.getKey());
                    System.out.println("Processing position: " + entry.getKey() + " -> " + dbPosition); // Debug output

                    // 1. Insert into stato_missili_missione table
                    PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO stato_missili_missione (ID_Missione, PosizioneVelivolo, " +
                                    "Lanciatore_PartNumber, Lanciatore_SerialNumber, PartNumber, Nomenclatura, Stato) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?)"
                    );

                    stmt.setInt(1, missionId);
                    stmt.setString(2, dbPosition);
                    stmt.setString(3, config.getLauncherId());
                    stmt.setString(4, config.getLauncherSerialNumber());
                    stmt.setString(5, config.getWeaponId());

                    // Get weapon nomenclatura from DB
                    Weapon weapon = weaponDAO.getByPartNumber(config.getWeaponId());
                    stmt.setString(6, weapon != null ? weapon.getNomenclatura() : "Unknown");

                    // Default status is "A_BORDO" (on board)
                    stmt.setString(7, "A_BORDO");

                    stmt.executeUpdate();
                    stmt.close();
                    System.out.println("Added to stato_missili_missione"); // Debug output

                    // 2. Also insert into storico_carico and storico_lanciatore tables
                    // Insert into storico_lanciatore
                    stmt = conn.prepareStatement(
                            "INSERT INTO storico_lanciatore (MatricolaVelivolo, PosizioneVelivolo, " +
                                    "PartNumber, SerialNumber, DataInstallazione, DataRimozione) " +
                                    "VALUES (?, ?, ?, ?, ?, NULL)"
                    );

                    stmt.setString(1, mission.getMatricolaVelivolo());
                    stmt.setString(2, dbPosition);
                    stmt.setString(3, config.getLauncherId());
                    stmt.setString(4, config.getLauncherSerialNumber());
                    stmt.setDate(5, mission.getDataMissione());

                    stmt.executeUpdate();
                    stmt.close();
                    System.out.println("Added to storico_lanciatore"); // Debug output

                    // Insert into storico_carico - updated to match actual table columns
                    stmt = conn.prepareStatement(
                            "INSERT INTO storico_carico (MatricolaVelivolo, PosizioneVelivolo, " +
                                    "PartNumber, SerialNumber, DataInstallazione, DataRimozione) " +
                                    "VALUES (?, ?, ?, ?, ?, NULL)"
                    );

                    // Updated parameters to match correct column order and types
                    stmt.setString(1, mission.getMatricolaVelivolo());
                    stmt.setString(2, dbPosition);
                    stmt.setString(3, config.getWeaponId());
                    // Generate a serial number for the weapon if needed
                    stmt.setString(4, "SN-" + config.getWeaponId().substring(0, Math.min(4, config.getWeaponId().length())) + "-" +
                            String.format("%03d", new Random().nextInt(999)));
                    stmt.setDate(5, mission.getDataMissione());

                    stmt.executeUpdate();
                    stmt.close();
                    System.out.println("Added to storico_carico"); // Debug output
                }

                // Commit transaction
                conn.commit();
                success = true;

                String message = "Mission saved successfully";
                if (loadedPositions > 0) {
                    message += " with " + loadedPositions + " configured positions";
                }

                AlertUtils.showInformation(owner, "Success", message);

                // Clear form and reset UI
                clearForm();
            } else {
                conn.rollback();
                AlertUtils.showError(owner, "Error", "Failed to save mission");
                success = false;
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            AlertUtils.showError(owner, "Database Error", "Error saving mission: " + e.getMessage());
            e.printStackTrace();
            success = false;
        } finally {
            // Close resources
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
