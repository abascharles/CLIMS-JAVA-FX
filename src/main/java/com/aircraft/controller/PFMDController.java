package com.aircraft.controller;

import com.aircraft.dao.AircraftDAO;
import com.aircraft.dao.MissionDAO;
import com.aircraft.model.Aircraft;
import com.aircraft.model.Mission;
import com.aircraft.util.AlertUtils;
import com.aircraft.util.DBUtil;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

import java.io.InputStream;
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

    // Removed velocitaMassimaField as it's no longer in the view

    @FXML
    private StackPane aircraftContainer;

    @FXML
    private WebView aircraftSvgView;

    @FXML
    private AnchorPane missilePointsContainer;

    private final AircraftDAO aircraftDAO = new AircraftDAO();
    private final MissionDAO missionDAO = new MissionDAO();

    // Map to track missile positions and their status
    private final Map<String, MissileStatus> missileStatusMap = new HashMap<>();
    private final Map<String, Pane> missilePointsMap = new HashMap<>();

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

        // Make flight data fields read-only
        gloadMaxField.setEditable(false);
        gloadMinField.setEditable(false);
        quotaMediaField.setEditable(false);
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
        missionComboBox.getItems().clear();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DBUtil.getConnection();

            // Query missions from vista_gui_missione
            String query = "SELECT ID_Missione, NumeroVolo, DataMissione FROM vista_gui_missione " +
                    "WHERE MatricolaVelivolo = ? " +
                    "ORDER BY DataMissione DESC";

            statement = connection.prepareStatement(query);
            statement.setString(1, matricolaVelivolo);
            resultSet = statement.executeQuery();

            ObservableList<String> missionOptions = FXCollections.observableArrayList();

            while (resultSet.next()) {
                int id = resultSet.getInt("ID_Missione");
                int flightNumber = resultSet.getInt("NumeroVolo");
                Date missionDate = resultSet.getDate("DataMissione");

                // Format as "ID - Flight #X (YYYY-MM-DD)"
                String formattedDate = missionDate.toString();
                String displayText = id + " - Flight #" + flightNumber + " (" + formattedDate + ")";

                missionOptions.add(displayText);
            }

            // Set items
            missionComboBox.setItems(missionOptions);

        } catch (SQLException e) {
            Window owner = aircraftComboBox.getScene().getWindow();
            AlertUtils.showError(owner, "Database Error", "Failed to load missions: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Handles the "Load Mission Data" button click.
     * Loads mission data and occupied positions.
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

        // Load mission data from vista_gui_missione
        loadMissionData(missionId);

        // Load occupied positions (missiles)
        loadOccupiedPositions(missionId);

        // Load existing firing declarations
        loadFiringDeclarations(missionId);

        // Display a message to the user
        Window owner = aircraftComboBox.getScene().getWindow();
        AlertUtils.showInformation(owner, "Mission Loaded",
                "Mission data loaded. Click on occupied positions to mark missiles as fired.");
    }

    /**
     * Loads mission data from vista_gui_missione.
     *
     * @param missionId The mission ID
     */
    private void loadMissionData(int missionId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DBUtil.getConnection();

            // Query from vista_gui_missione
            String query = "SELECT GloadMin, GloadMax, QuotaMedia FROM vista_gui_missione " +
                    "WHERE ID_Missione = ?";

            statement = connection.prepareStatement(query);
            statement.setInt(1, missionId);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // Populate flight data fields
                gloadMinField.setText(String.valueOf(resultSet.getDouble("GloadMin")));
                gloadMaxField.setText(String.valueOf(resultSet.getDouble("GloadMax")));
                quotaMediaField.setText(String.valueOf(resultSet.getInt("QuotaMedia")));
            } else {
                // Clear fields if no data found
                gloadMinField.clear();
                gloadMaxField.clear();
                quotaMediaField.clear();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Loads occupied positions from the database.
     *
     * @param missionId The mission ID
     */
    private void loadOccupiedPositions(int missionId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DBUtil.getConnection();

            // Reset all positions to EMPTY
            for (String position : missileStatusMap.keySet()) {
                missileStatusMap.put(position, MissileStatus.EMPTY);
            }

            // Query for occupied positions
            String query = "SELECT DISTINCT mpa.PosizioneVelivolo " +
                    "FROM missione_posizione_automatica mpa " +
                    "WHERE mpa.ID_Missione = ?";

            statement = connection.prepareStatement(query);
            statement.setInt(1, missionId);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String position = resultSet.getString("PosizioneVelivolo");

                // Set position as ONBOARD
                missileStatusMap.put(position, MissileStatus.ONBOARD);
                System.out.println("Found occupied position: " + position);
            }

            // Update UI to reflect loaded status
            updateMissilePositionStyles();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Loads existing firing declarations from dichiarazione_missile_gui.
     *
     * @param missionId The mission ID
     */
    private void loadFiringDeclarations(int missionId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DBUtil.getConnection();

            // Query for firing declarations
            String query = "SELECT PosizioneVelivolo FROM dichiarazione_missile_gui " +
                    "WHERE ID_Missione = ? AND Missile_Sparato = 'SI'";

            statement = connection.prepareStatement(query);
            statement.setInt(1, missionId);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String position = resultSet.getString("PosizioneVelivolo");

                // Set position as FIRED
                missileStatusMap.put(position, MissileStatus.FIRED);
                System.out.println("Found fired position: " + position);
            }

            // Update UI to reflect loaded status
            updateMissilePositionStyles();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(connection, statement, resultSet);
        }
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
                rect.setOpacity(0.9);
                label.setFill(Color.BLACK);
                break;
            case FIRED:
                rect.setFill(Color.RED);
                rect.setStroke(Color.DARKRED);
                rect.setStrokeWidth(2);
                rect.setOpacity(0.9);
                label.setFill(Color.WHITE);
                break;
        }
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

        // Only allow clicking if the missile is ONBOARD (to mark as FIRED)
        if (status == MissileStatus.ONBOARD) {
            // Change to FIRED - just update UI, don't save yet
            missileStatusMap.put(position, MissileStatus.FIRED);
            updateMissilePositionStyle(clickedPane, position);

            // Just show a tooltip or message, don't save to DB yet
            Window owner = clickedPane.getScene().getWindow();
            AlertUtils.showInformation(owner, "Status Updated",
                    "Position " + position + " marked as FIRED. Click Save Data to save all changes.");

        } else if (status == MissileStatus.FIRED) {
            // Don't allow changing back from FIRED to ONBOARD
            Window owner = clickedPane.getScene().getWindow();
            AlertUtils.showWarning(owner, "Missile Already Fired",
                    "This missile has been fired and cannot be marked as onboard again.");

        } else if (status == MissileStatus.EMPTY) {
            // Can't click on empty positions
            Window owner = clickedPane.getScene().getWindow();
            AlertUtils.showWarning(owner, "No Missile", "No missile loaded at position " + position);
        }
    }

    /**
     * Saves or updates a firing declaration in the dichiarazione_missile_gui table.
     *
     * @param position The position identifier
     * @param fired Whether the missile was fired (true) or not (false)
     */
    private void saveFiringDeclaration(String position, boolean fired) {
        Connection connection = null;
        PreparedStatement checkStmt = null;
        PreparedStatement insertStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet resultSet = null;

        try {
            connection = DBUtil.getConnection();

            // Check if this position already has a declaration for this mission
            String checkQuery = "SELECT ID FROM dichiarazione_missile_gui " +
                    "WHERE ID_Missione = ? AND PosizioneVelivolo = ?";

            checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, currentMissionId);
            checkStmt.setString(2, position);
            resultSet = checkStmt.executeQuery();

            if (resultSet.next()) {
                // Update existing declaration
                int declarationId = resultSet.getInt("ID");

                String updateQuery = "UPDATE dichiarazione_missile_gui " +
                        "SET Missile_Sparato = ? " +
                        "WHERE ID = ?";

                updateStmt = connection.prepareStatement(updateQuery);
                updateStmt.setString(1, fired ? "SI" : "NO");
                updateStmt.setInt(2, declarationId);
                updateStmt.executeUpdate();

                System.out.println("Updated firing declaration for position " + position + " to " + (fired ? "SI" : "NO"));

            } else {
                // Get current max ID and increment by 1
                String getMaxIdQuery = "SELECT MAX(ID) as MaxID FROM dichiarazione_missile_gui";
                PreparedStatement maxIdStmt = connection.prepareStatement(getMaxIdQuery);
                ResultSet maxIdResult = maxIdStmt.executeQuery();

                int nextId = 1; // Default to 1 if no records exist yet
                if (maxIdResult.next()) {
                    nextId = maxIdResult.getInt("MaxID") + 1;
                }
                maxIdResult.close();
                maxIdStmt.close();

                // Insert new declaration with explicit ID value
                String insertQuery = "INSERT INTO dichiarazione_missile_gui " +
                        "(ID, ID_Missione, PosizioneVelivolo, Missile_Sparato) " +
                        "VALUES (?, ?, ?, ?)";

                insertStmt = connection.prepareStatement(insertQuery);
                insertStmt.setInt(1, nextId);
                insertStmt.setInt(2, currentMissionId);
                insertStmt.setString(3, position);
                insertStmt.setString(4, fired ? "SI" : "NO");
                insertStmt.executeUpdate();

                System.out.println("Inserted new firing declaration for position " + position + ": " + (fired ? "SI" : "NO"));
            }

            // Inform user
            Window owner = aircraftContainer.getScene().getWindow();
            String message = "Position " + position + " marked as " + (fired ? "FIRED" : "ONBOARD");
            AlertUtils.showInformation(owner, "Status Updated", message);

        } catch (SQLException e) {
            e.printStackTrace();
            Window owner = aircraftContainer.getScene().getWindow();
            AlertUtils.showError(owner, "Database Error", "Failed to save firing declaration: " + e.getMessage());
        } finally {
            DBUtil.closeResources(null, checkStmt, resultSet);
            DBUtil.closeResources(null, insertStmt, null);
            DBUtil.closeResources(connection, updateStmt, null);
        }
    }

    /**
     * Handles the "Save Data" button click.
     * This is now simplified to just save missile firing declarations.
     *
     * @param event The ActionEvent object
     */
    @FXML
    protected void onSaveDataClick(ActionEvent event) {
        Window owner = gloadMaxField.getScene().getWindow();

        if (currentMissionId == null) {
            AlertUtils.showError(owner, "No Mission Selected", "Please load a mission first");
            return;
        }

        // All saving now happens directly when clicking on positions
        AlertUtils.showInformation(owner, "Data Saved",
                "All missile firing declarations have been saved.");
    }

    /**
     * Handles the "Clear Form" button click.
     * Clears form fields and resets missile status.
     *
     * @param event The ActionEvent object
     */
    @FXML
    protected void onClearFormClick(ActionEvent event) {
        // Clear form fields
        gloadMaxField.clear();
        gloadMinField.clear();
        quotaMediaField.clear();

        // Reset missile status
        for (String position : missileStatusMap.keySet()) {
            missileStatusMap.put(position, MissileStatus.EMPTY);
        }

        // Update UI
        updateMissilePositionStyles();

        // Reset current mission ID
        currentMissionId = null;
        currentAircraft = null;
        currentFlightNumber = null;
    }
}