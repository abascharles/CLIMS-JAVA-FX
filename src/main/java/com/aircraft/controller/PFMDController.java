package com.aircraft.controller;

import com.aircraft.dao.AircraftDAO;
import com.aircraft.dao.MissionDAO;
import com.aircraft.dao.RecordedDataDAO;
import com.aircraft.model.Aircraft;
import com.aircraft.model.Mission;
import com.aircraft.model.RecordedData;
import com.aircraft.util.AlertUtils;
import com.aircraft.util.DBUtil;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Post Flight Management Data (PFMD) module.
 * Handles recording and updating flight data after missions.
 */
public class PFMDController {
    @FXML
    private ComboBox<String> aircraftComboBox;

    @FXML
    private ComboBox<String> missionComboBox;

    @FXML
    private TextField gloadMaxField;

    @FXML
    private TextField gloadMinField;

    @FXML
    private TextField quotaMediaField;

    @FXML
    private TextField velocitaMassimaField;

    @FXML
    private StackPane aircraftContainer;

    @FXML
    private WebView aircraftSvgView;

    @FXML
    private AnchorPane missilePointsContainer;

    private final AircraftDAO aircraftDAO = new AircraftDAO();
    private final MissionDAO missionDAO = new MissionDAO();
    private final RecordedDataDAO recordedDataDAO = new RecordedDataDAO();

    // Map to track missile positions and their status
    private final Map<String, MissileStatus> missileStatusMap = new HashMap<>();
    private final Map<String, Pane> missilePointsMap = new HashMap<>();

    // Map to track loaded weapons based on mission
    private Map<String, Map<String, String>> loadedWeapons = new HashMap<>();

    // Current selected mission ID
    private Integer currentMissionId;
    private String currentAircraft;
    private Integer currentFlightNumber;

    /**
     * Enumeration for missile status
     */
    private enum MissileStatus {
        EMPTY,      // No missile loaded
        ONBOARD,    // Missile loaded and onboard
        FIRED       // Missile was fired
    }

    /**
     * Initializes the controller after its root element has been processed.
     * Sets up event handlers and initializes UI components.
     */
    @FXML
    public void initialize() {
        // Load aircraft data
        loadAircraftData();

        // Set up event handler for aircraft selection
        aircraftComboBox.setOnAction(event -> {
            String selectedAircraft = aircraftComboBox.getValue();
            if (selectedAircraft != null) {
                currentAircraft = selectedAircraft;
                loadMissions(selectedAircraft);
            }
        });

        // Initialize missile status map
        initializeMissileStatusMap();

        // Load SVG after the WebView is fully initialized
        Platform.runLater(this::loadAircraftSvg);
    }

    /**
     * Initializes the missile status map with all positions.
     */
    private void initializeMissileStatusMap() {
        // Initialize with 13 missile positions
        for (int i = 1; i <= 13; i++) {
            missileStatusMap.put("P" + i, MissileStatus.EMPTY);
        }
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
        // Define positions based on SVG coordinates
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

        // Clear any existing missile points
        missilePointsContainer.getChildren().clear();
        missilePointsMap.clear();

        // Create visual indicators for each position
        for (Map.Entry<String, double[]> entry : positions.entrySet()) {
            String position = entry.getKey();
            double[] coords = entry.getValue();

            // Create a missile point indicator (rectangle)
            Rectangle point = new Rectangle(24, 40);

            // Set initial style - transparent with light gray border
            point.setFill(Color.TRANSPARENT);
            point.setStroke(Color.LIGHTGRAY);
            point.setStrokeWidth(1);
            point.setOpacity(0.7);

            // Create position label
            Text label = new Text(position);
            label.setFill(Color.BLACK);
            label.getStyleClass().add("missile-point-label");

            // Combine in a StackPane
            StackPane missilePoint = new StackPane(point, label);
            missilePoint.setLayoutX(coords[0] - 12); // Center the rectangle
            missilePoint.setLayoutY(coords[1] - 20);
            missilePoint.setUserData(position); // Store position ID for click handler

            // Add click event
            missilePoint.setOnMouseClicked(this::onMissilePositionClick);

            // Store for later reference
            missilePointsMap.put(position, missilePoint);

            // Add to container
            missilePointsContainer.getChildren().add(missilePoint);
        }
    }

    /**
     * Loads aircraft data into the aircraft combo box.
     */
    private void loadAircraftData() {
        List<Aircraft> aircraftList = aircraftDAO.getAll();
        ObservableList<String> aircraftOptions = FXCollections.observableArrayList();

        for (Aircraft aircraft : aircraftList) {
            aircraftOptions.add(aircraft.getMatricolaVelivolo());
        }

        aircraftComboBox.setItems(aircraftOptions);
    }

    /**
     * Loads available missions for an aircraft.
     *
     * @param matricolaVelivolo The aircraft serial number
     */
    private void loadMissions(String matricolaVelivolo) {
        // Clear existing items first
        missionComboBox.getItems().clear();

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DBUtil.getConnection();

            // Direct SQL query with no caching
            String query = "SELECT ID, NumeroVolo, DataMissione FROM missione " +
                    "WHERE MatricolaVelivolo = '" + matricolaVelivolo + "' " +
                    "ORDER BY DataMissione DESC";

            statement = connection.createStatement();
            statement.setFetchSize(Integer.MIN_VALUE); // Disable result set caching
            resultSet = statement.executeQuery(query);

            ObservableList<String> missionOptions = FXCollections.observableArrayList();

            while (resultSet.next()) {
                int id = resultSet.getInt("ID");
                int flightNumber = resultSet.getInt("NumeroVolo");
                Date missionDate = resultSet.getDate("DataMissione");

                // Format as "ID - Flight #X (YYYY-MM-DD)"
                String formattedDate = missionDate.toString();
                String displayText = id + " - Flight #" + flightNumber + " (" + formattedDate + ")";

                missionOptions.add(displayText);

                System.out.println("Loaded mission: " + displayText); // Debug output
            }

            // Set items in a new thread to avoid JavaFX thread issues
            Platform.runLater(() -> {
                missionComboBox.setItems(null);
                missionComboBox.setItems(missionOptions);
                missionComboBox.requestLayout();

                // Force a visual refresh
                missionComboBox.setVisible(false);
                missionComboBox.setVisible(true);
            });

        } catch (SQLException e) {
            Window owner = aircraftComboBox.getScene().getWindow();
            AlertUtils.showError(owner, "Database Error", "Failed to load missions: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Loads the weapons/missiles configuration for a mission.
     * This method directly queries the database tables to determine which positions have loaded missiles.
     *
     * @param missionId The mission ID
     */
    private void loadMissionWeapons(int missionId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DBUtil.getConnection();

            // Clear existing data
            loadedWeapons.clear();

            // Reset missile status map - all positions start as EMPTY
            for (String position : missileStatusMap.keySet()) {
                missileStatusMap.put(position, MissileStatus.EMPTY);
            }

            // Get the mission details
            String missionQuery = "SELECT * FROM missione WHERE ID = ?";
            statement = connection.prepareStatement(missionQuery);
            statement.setInt(1, missionId);
            resultSet = statement.executeQuery();

            Mission mission = null;
            if (resultSet.next()) {
                mission = new Mission();
                mission.setId(resultSet.getInt("ID"));
                mission.setMatricolaVelivolo(resultSet.getString("MatricolaVelivolo"));
                mission.setNumeroVolo(resultSet.getInt("NumeroVolo"));
                mission.setDataMissione(resultSet.getDate("DataMissione"));
            }

            if (mission == null) {
                // No mission found, can't proceed
                return;
            }

            // Close resources for next query
            DBUtil.closeResources(null, statement, resultSet);

            // First, check stato_missili_missione table
            String missileStatusQuery = "SELECT * FROM stato_missili_missione WHERE ID_Missione = ?";
            statement = connection.prepareStatement(missileStatusQuery);
            statement.setInt(1, missionId);
            resultSet = statement.executeQuery();

            boolean foundMissileStatus = false;

            while (resultSet.next()) {
                foundMissileStatus = true;
                String position = resultSet.getString("PosizioneVelivolo");
                String partNumber = resultSet.getString("PartNumber");
                String stato = resultSet.getString("Stato");
                String launcherPN = resultSet.getString("Lanciatore_PartNumber");

                // Map database position to UI position
                String uiPosition = mapDbPositionToUiPosition(position);

                if (uiPosition != null) {
                    // Create data map for position
                    Map<String, String> weaponData = new HashMap<>();
                    weaponData.put("position", position);
                    weaponData.put("partNumber", partNumber);
                    weaponData.put("status", stato);
                    weaponData.put("launcherPN", launcherPN);

                    loadedWeapons.put(uiPosition, weaponData);

                    // Update missile status based on state
                    if (stato.equals("A_BORDO")) {
                        missileStatusMap.put(uiPosition, MissileStatus.ONBOARD);
                    } else if (stato.equals("SPARATO")) {
                        missileStatusMap.put(uiPosition, MissileStatus.FIRED);
                    }

                    System.out.println("Loaded missile at " + uiPosition + " with status " + missileStatusMap.get(uiPosition));
                }
            }

            // If no records found in stato_missili_missione, check storico_carico
            if (!foundMissileStatus) {
                // Close resources for next query
                DBUtil.closeResources(null, statement, resultSet);

                // First get launcher info
                String launcherQuery = "SELECT * FROM storico_lanciatore WHERE MatricolaVelivolo = ? " +
                        "AND DataInstallazione <= ? " +
                        "AND (DataRimozione IS NULL OR DataRimozione >= ?)";

                statement = connection.prepareStatement(launcherQuery);
                statement.setString(1, mission.getMatricolaVelivolo());
                statement.setDate(2, mission.getDataMissione());
                statement.setDate(3, mission.getDataMissione());
                resultSet = statement.executeQuery();

                // Store launcher info
                Map<String, Map<String, String>> launcherMap = new HashMap<>();

                while (resultSet.next()) {
                    String position = resultSet.getString("PosizioneVelivolo");
                    String partNumber = resultSet.getString("PartNumber");
                    String serialNumber = resultSet.getString("SerialNumber");

                    Map<String, String> launcherInfo = new HashMap<>();
                    launcherInfo.put("partNumber", partNumber);
                    launcherInfo.put("serialNumber", serialNumber);

                    launcherMap.put(position, launcherInfo);
                }

                // Close resources for next query
                DBUtil.closeResources(null, statement, resultSet);

                // Now get missile info from storico_carico
                String missileQuery = "SELECT * FROM storico_carico WHERE DataImbarco <= ? " +
                        "AND (DataSbarco IS NULL OR DataSbarco >= ?)";

                statement = connection.prepareStatement(missileQuery);
                statement.setDate(1, mission.getDataMissione());
                statement.setDate(2, mission.getDataMissione());
                resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    String position = resultSet.getString("PosizioneVelivolo");
                    String partNumber = resultSet.getString("PartNumber");
                    String nomenclatura = resultSet.getString("Nomenclatura");

                    // If this position has a launcher, then consider it loaded
                    if (launcherMap.containsKey(position)) {
                        String uiPosition = mapDbPositionToUiPosition(position);

                        if (uiPosition != null) {
                            // Create weapon data
                            Map<String, String> weaponData = new HashMap<>();
                            weaponData.put("position", position);
                            weaponData.put("partNumber", partNumber);
                            weaponData.put("nomenclatura", nomenclatura);
                            weaponData.put("launcherPN", launcherMap.get(position).get("partNumber"));
                            weaponData.put("launcherSN", launcherMap.get(position).get("serialNumber"));

                            loadedWeapons.put(uiPosition, weaponData);

                            // Mark as ONBOARD
                            missileStatusMap.put(uiPosition, MissileStatus.ONBOARD);

                            System.out.println("Loaded missile at " + uiPosition + " from storico_carico");
                        }
                    }
                }
            }

            // Check for existing recorded data
            DBUtil.closeResources(null, statement, resultSet);
            RecordedData recordedData = recordedDataDAO.getByFlightNumber(mission.getMatricolaVelivolo(), mission.getNumeroVolo());

            if (recordedData != null && recordedData.getStatoMissili() != null) {
                // Parse the status string and apply it
                parseStatoMissili(recordedData.getStatoMissili());
            }

            // Debug info
            debugPrintMissileStatus();
            debugPrintLoadedWeapons();

            // Update UI to show loaded missiles
            updateMissilePositionStyles();

        } catch (SQLException e) {
            Window owner = aircraftComboBox.getScene().getWindow();
            AlertUtils.showError(owner, "Database Error", "Failed to load mission weapons: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Maps database position code to UI position identifier.
     * This method converts between various position naming conventions in the database
     * and the UI position identifiers (P1-P13).
     *
     * @param dbPosition The database position code
     * @return The corresponding UI position identifier (P1-P13)
     */
    private String mapDbPositionToUiPosition(String dbPosition) {
        if (dbPosition == null) return null;

        // Simple mapping for exact position matches
        if (dbPosition.matches("P\\d+")) {
            return dbPosition;
        }

        // Map your custom positions to P1-P13
        switch (dbPosition) {
            case "ADAtre1": return "P1";
            case "ADAtre2": return "P2";
            case "ADAtre3": return "P3";
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
            default: return null;
        }
    }

    /**
     * Maps UI position identifier to database position code.
     * This is the reverse of mapDbPositionToUiPosition.
     *
     * @param uiPosition The UI position identifier (P1-P13)
     * @return The corresponding database position code
     */
    private String mapUiPositionToDbPosition(String uiPosition) {
        // Get mapped position from loaded weapons if available
        if (loadedWeapons.containsKey(uiPosition)) {
            return loadedWeapons.get(uiPosition).get("position");
        }

        // Otherwise, use default mapping
        switch (uiPosition) {
            case "P1": return "ADAtre1";
            case "P2": return "ADAtre2";
            case "P3": return "ADAtre3";
            case "P4": return "P4";
            case "P5": return "P5";
            case "P6": return "P6";
            case "P7": return "P7";
            case "P8": return "P8";
            case "P9": return "P9";
            case "P10": return "P10";
            case "P11": return "P11";
            case "P12": return "P12";
            case "P13": return "P13";
            default: return uiPosition;
        }
    }

    /**
     * Parses the StatoMissili string from the database and updates the missileStatusMap.
     * Format example: "P1:SPARATO; P2:A_BORDO"
     *
     * @param statoMissili The status string to parse
     */
    private void parseStatoMissili(String statoMissili) {
        if (statoMissili == null || statoMissili.isEmpty()) return;

        String[] entries = statoMissili.split(";");
        for (String entry : entries) {
            entry = entry.trim();
            if (entry.isEmpty()) continue;

            String[] parts = entry.split(":");
            if (parts.length == 2) {
                String position = parts[0].trim();
                String status = parts[1].trim();

                // Map status string to enum
                if (status.equals("SPARATO")) {
                    missileStatusMap.put(position, MissileStatus.FIRED);
                    System.out.println("Set " + position + " to FIRED from parseStatoMissili");
                } else if (status.equals("A_BORDO")) {
                    missileStatusMap.put(position, MissileStatus.ONBOARD);
                    System.out.println("Set " + position + " to ONBOARD from parseStatoMissili");
                }
            }
        }
    }

    /**
     * Handles the "Load Mission Data" button click.
     * Loads mission data and weapon configuration.
     *
     * @param event The ActionEvent object
     */
    @FXML
    protected void onLoadMissionDataClick(ActionEvent event) {
        String selectedAircraft = aircraftComboBox.getValue();
        String selectedMissionStr = missionComboBox.getValue();

        if (selectedAircraft == null || selectedMissionStr == null) {
            Window owner = aircraftComboBox.getScene().getWindow();
            AlertUtils.showError(owner, "Selection Error", "Please select both an aircraft and a mission");
            return;
        }

        // Extract mission ID from the selection (format: "ID - Flight #XX...")
        String idPart = selectedMissionStr.split(" - ")[0];
        int missionId = Integer.parseInt(idPart);
        currentMissionId = missionId;
        currentAircraft = selectedAircraft;

        // Extract flight number from the selection (format: "ID - Flight #XX...")
        String flightPart = selectedMissionStr.split(" - ")[1];
        currentFlightNumber = Integer.parseInt(flightPart.substring(flightPart.indexOf("#") + 1).split(" ")[0]);

        // Load mission weapons configuration - this will show green squares for loaded missiles
        loadMissionWeapons(missionId);

        // Clear form fields - user will enter these manually
        clearFormFields();

        // Display a message to the user
        Window owner = aircraftComboBox.getScene().getWindow();
        AlertUtils.showInformation(owner, "Mission Loaded",
            "Missile configurations loaded. Please fill in flight data and mark fired missiles.");
    }
    /**
     * Updates the styles of all missile positions based on their current status.
     */
    private void updateMissilePositionStyles() {
        for (Map.Entry<String, Pane> entry : missilePointsMap.entrySet()) {
            String position = entry.getKey();
            Pane pane = entry.getValue();
            updateMissilePositionStyle(pane, position);
        }
    }

    /**
     * Loads existing recorded data for the selected aircraft and flight number.
     *
     * @param aircraft The aircraft serial number
     * @param flightNumber The flight number
     */
    private void loadRecordedData(String aircraft, int flightNumber) {
        RecordedData data = recordedDataDAO.getByFlightNumber(aircraft, flightNumber);

        if (data != null) {
            // DO NOT fill form fields with existing data - let user enter manually
            // Instead clear the form fields
            clearFormFields();

            // Only parse and apply missile status if it exists
            if (data.getStatoMissili() != null && !data.getStatoMissili().isEmpty()) {
                parseStatoMissili(data.getStatoMissili());
                updateMissilePositionStyles();
            }
        } else {
            // Clear form fields for new data
            clearFormFields();
        }
    }

    /**
     * Clears the form fields.
     */
    private void clearFormFields() {
        gloadMaxField.clear();
        gloadMinField.clear();
        quotaMediaField.clear();
        velocitaMassimaField.clear();
    }

   /**
    * Updates the style of a single missile position pane.
    * This method applies styles directly to the Rectangle rather than using CSS classes.
    *
    * @param pane The Pane object
    * @param position The position identifier
    */

    /**
     * Updates the style of a single missile position pane.
     * This method applies styles directly to the Rectangle rather than using CSS classes.
     *
     * @param pane The Pane object
     * @param position The position identifier
     */
    private void updateMissilePositionStyle(Pane pane, String position) {
        if (pane == null) return;

        // Get status for this position
        MissileStatus status = missileStatusMap.get(position);
        if (status == null) status = MissileStatus.EMPTY;

        // Get the rectangle from the StackPane
        Rectangle rect = (Rectangle) ((StackPane) pane).getChildren().get(0);
        Text label = (Text) ((StackPane) pane).getChildren().get(1);

        // Apply styles directly to the rectangle based on status
        switch (status) {
            case EMPTY:
                rect.setFill(Color.TRANSPARENT);
                rect.setStroke(Color.LIGHTGRAY);
                rect.setStrokeWidth(1);
                rect.setOpacity(0.7);
                label.setFill(Color.BLACK);
                break;
            case ONBOARD:
                rect.setFill(Color.LIGHTGREEN);
                rect.setStroke(Color.GREEN);
                rect.setStrokeWidth(2);
                rect.setOpacity(1.0);
                label.setFill(Color.BLACK);
                break;
            case FIRED:
                rect.setFill(Color.RED);
                rect.setStroke(Color.DARKRED);
                rect.setStrokeWidth(2);
                rect.setOpacity(1.0);
                label.setFill(Color.WHITE);
                break;
        }

        System.out.println("Updated style for " + position + " to " + status);
    }

    /**
     * Handles clicks on missile position panes.
     * Toggles the status of the clicked missile position.
     *
     * @param event The MouseEvent object
     */
    @FXML
    protected void onMissilePositionClick(MouseEvent event) {
        if (currentMissionId == null) {
            Window owner = aircraftComboBox.getScene().getWindow();
            AlertUtils.showWarning(owner, "No Mission Selected", "Please load a mission first");
            return;
        }

        StackPane clickedPane = (StackPane) event.getSource();
        String position = (String) clickedPane.getUserData();

        // Get current status
        MissileStatus status = missileStatusMap.get(position);
        Window owner = clickedPane.getScene().getWindow();

        // Only allow clicking if the missile is ONBOARD (to mark as FIRED)
        if (status == MissileStatus.ONBOARD) {
            // Change to FIRED
            missileStatusMap.put(position, MissileStatus.FIRED);
            updateMissilePositionStyle(clickedPane, position);
            AlertUtils.showInformation(owner, "Missile Fired",
                    "Missile at position " + position + " has been marked as FIRED");
        } else if (status == MissileStatus.EMPTY) {
            // Can't click on empty positions
            AlertUtils.showWarning(owner, "No Missile", "No missile loaded at position " + position);
        }
    }

    /**
     * Builds a missile status string from the current state of the status map.
     * Format: "P1:SPARATO; P2:A_BORDO"
     *
     * @return The formatted missile status string
     */
    private String buildMissileStatusString() {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, MissileStatus> entry : missileStatusMap.entrySet()) {
            String position = entry.getKey();
            MissileStatus status = entry.getValue();

            // Only include non-empty positions
            if (status != MissileStatus.EMPTY) {
                if (sb.length() > 0) {
                    sb.append("; ");
                }

                sb.append(position).append(":");

                if (status == MissileStatus.FIRED) {
                    sb.append("SPARATO");
                } else {
                    sb.append("A_BORDO");
                }
            }
        }

        return sb.toString();
    }

    /**
     * Handles the "Save Data" button click.
     * Validates and saves the recorded flight data.
     *
     * @param event The ActionEvent object
     */
    @FXML
    protected void onSaveDataClick(ActionEvent event) {
        Window owner = gloadMaxField.getScene().getWindow();

        if (currentMissionId == null || currentFlightNumber == null) {
            AlertUtils.showError(owner, "No Mission Selected", "Please load a mission first");
            return;
        }

        // Validate numeric fields
        BigDecimal gloadMax, gloadMin;
        Integer quotaMedia, velocitaMassima;

        try {
            gloadMax = new BigDecimal(gloadMaxField.getText().replace(',', '.'));
            gloadMin = new BigDecimal(gloadMinField.getText().replace(',', '.'));
            quotaMedia = Integer.parseInt(quotaMediaField.getText());
            velocitaMassima = Integer.parseInt(velocitaMassimaField.getText());
        } catch (NumberFormatException e) {
            AlertUtils.showError(owner, "Validation Error", "Please enter valid numeric values for all fields");
            return;
        }

        // Create or update recorded data
        RecordedData existingData = recordedDataDAO.getByFlightNumber(currentAircraft, currentFlightNumber);
        RecordedData recordedData;

        if (existingData != null) {
            // Update existing record
            recordedData = existingData;
        } else {
            // Create new record
            recordedData = new RecordedData();
            recordedData.setMatricolaVelivolo(currentAircraft);
            recordedData.setNumeroVolo(currentFlightNumber);
        }

        recordedData.setGloadMax(gloadMax);
        recordedData.setGloadMin(gloadMin);
        recordedData.setQuotaMedia(quotaMedia);
        recordedData.setVelocitaMassima(velocitaMassima);
        recordedData.setStatoMissili(buildMissileStatusString());
        recordedData.setStatoElaborato(true);

        // Save data to database
        boolean success;
        if (existingData != null) {
            success = recordedDataDAO.update(recordedData);
        } else {
            success = recordedDataDAO.insert(recordedData);
        }

        // Show appropriate message
        if (success) {
            AlertUtils.showInformation(owner, "Success", "Flight data saved successfully");

            // Critical: Complete refresh to ensure new missions appear
            Platform.runLater(() -> {
                // Clear caches and reload everything to ensure we see new missions
                Connection connection = null;
                try {
                    connection = DBUtil.getConnection();
                    // Force cache flush (dummy query)
                    try (Statement stmt = connection.createStatement()) {
                        stmt.execute("SELECT 1");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    DBUtil.closeResources(connection, null, null);
                }

                // Store current selection
                String currentAircraftVal = aircraftComboBox.getValue();

                // Reset and reload aircraft
                aircraftComboBox.getItems().clear();
                loadAircraftData();

                // Restore selection and reload missions
                aircraftComboBox.setValue(currentAircraftVal);
                loadMissions(currentAircraftVal);

                // Force UI update
                missilePointsContainer.requestLayout();
                aircraftComboBox.requestLayout();
                missionComboBox.requestLayout();
            });
        } else {
            AlertUtils.showError(owner, "Error", "Failed to save flight data");
        }
    }

    /**
     * Refreshes the mission list.
     */
    @FXML
    private void refreshMissions() {
        if (currentAircraft != null) {
            loadMissions(currentAircraft);
        }
    }

    /**
     * Handles the "Clear Form" button click.
     * Clears all form fields and resets missile status.
     *
     * @param event The ActionEvent object
     */
    @FXML
    protected void onClearFormClick(ActionEvent event) {
        clearForm();
    }

    /**
     * Clears all form fields and resets missile status.
     */
    private void clearForm() {
        clearFormFields();

        // Reset missile status to original configuration if mission is still loaded
        if (currentMissionId != null) {
            loadMissionWeapons(currentMissionId);
        } else {
            // Reset missile status
            for (String position : missileStatusMap.keySet()) {
                missileStatusMap.put(position, MissileStatus.EMPTY);
            }

            // Clear loaded weapons
            loadedWeapons.clear();

            // Update UI
            updateMissilePositionStyles();
        }
    }

    /**
     * Prints debug information about all missile statuses
     */
    private void debugPrintMissileStatus() {
        System.out.println("\n--- MISSILE STATUS DEBUG ---");
        for (Map.Entry<String, MissileStatus> entry : missileStatusMap.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
        System.out.println("-------------------------\n");
    }

    /**
     * Prints debug information about loaded weapons
     */
    private void debugPrintLoadedWeapons() {
        System.out.println("\n--- LOADED WEAPONS DEBUG ---");
        for (Map.Entry<String, Map<String, String>> entry : loadedWeapons.entrySet()) {
            System.out.println(entry.getKey() + ":");
            for (Map.Entry<String, String> dataEntry : entry.getValue().entrySet()) {
                System.out.println("  " + dataEntry.getKey() + " = " + dataEntry.getValue());
            }
        }
        System.out.println("-------------------------\n");
    }
}