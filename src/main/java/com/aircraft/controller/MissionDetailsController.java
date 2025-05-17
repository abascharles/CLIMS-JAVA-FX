package com.aircraft.controller;

import com.aircraft.dao.MissionDAO;
import com.aircraft.model.Mission;
import com.aircraft.model.WeaponStatus;
import com.aircraft.util.AlertUtils;
import com.aircraft.util.DBUtil;
import com.aircraft.util.PDFGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalTime;
import java.util.List;

/**
 * Controller for the Mission Details view.
 * Shows detailed information about a specific mission.
 */
public class MissionDetailsController {

    @FXML
    private Label missionIdLabel;

    @FXML
    private TextField aircraftField;

    @FXML
    private TextField flightNumberField;

    @FXML
    private TextField missionDateField;

    @FXML
    private TextField departureTimeField;

    @FXML
    private TextField arrivalTimeField;

    @FXML
    private TextField durationField;

    @FXML
    private TextField maxGLoadField;

    @FXML
    private TextField minGLoadField;

    @FXML
    private TextField avgAltitudeField;

    @FXML
    private TextField firedPositionsField;

    @FXML
    private TableView<WeaponStatus> weaponsTable;

    @FXML
    private TableColumn<WeaponStatus, String> positionColumn;

    @FXML
    private TableColumn<WeaponStatus, String> launcherPNColumn;

    @FXML
    private TableColumn<WeaponStatus, String> launcherSNColumn;

    @FXML
    private TableColumn<WeaponStatus, String> missilePNColumn;

    @FXML
    private TableColumn<WeaponStatus, String> missileNameColumn;

    @FXML
    private TableColumn<WeaponStatus, String> statusColumn;

    @FXML
    private Button closeButton;

    @FXML
    private Button exportButton;

    private final MissionDAO missionDAO = new MissionDAO();
    private Mission mission;
    private ObservableList<WeaponStatus> weaponsList = FXCollections.observableArrayList();

    /**
     * Initializes the controller after its root element has been processed.
     * Sets up UI components and table columns.
     */
    @FXML
    public void initialize() {
        // Set up table columns
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));
        launcherPNColumn.setCellValueFactory(new PropertyValueFactory<>("launcherPartNumber"));
        launcherSNColumn.setCellValueFactory(new PropertyValueFactory<>("launcherSerialNumber"));
        missilePNColumn.setCellValueFactory(new PropertyValueFactory<>("missilePartNumber"));
        missileNameColumn.setCellValueFactory(new PropertyValueFactory<>("missileName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Initialize weapons table
        weaponsTable.setItems(weaponsList);
    }

    /**
     * Sets the mission to be displayed and loads its details.
     *
     * @param missionId The ID of the mission to display
     */
    public void setMission(int missionId) {
        // Get full mission details from DAO
        mission = missionDAO.getMissionById(missionId);

        if (mission != null) {
            // Load mission data
            loadMissionData(missionId);

            // Load flight data from vista_gui_missione
            loadFlightDataFromView(missionId);

            // Load weapons data
            loadWeaponsData(missionId);
        } else {
            // Handle error - mission not found
            AlertUtils.showError(null, "Error", "Mission not found: ID " + missionId);
            clearAllFields();
        }
    }

    private void clearAllFields() {
        missionIdLabel.setText("Mission ID: #N/A");
        aircraftField.setText("");
        flightNumberField.setText("");
        missionDateField.setText("");
        departureTimeField.setText("");
        arrivalTimeField.setText("");
        durationField.setText("");
        maxGLoadField.setText("");
        minGLoadField.setText("");
        avgAltitudeField.setText("");
        firedPositionsField.setText("");
        weaponsList.clear();
    }

    /**
     * Loads basic mission information into form fields.
     */
    private void loadMissionData(int missionId) {
        missionIdLabel.setText("Mission ID: #" + mission.getId());
        aircraftField.setText(mission.getMatricolaVelivolo());
        flightNumberField.setText(String.valueOf(mission.getNumeroVolo()));
        missionDateField.setText(mission.getDataMissione().toString());

        // Set time fields
        Time departureTime = mission.getOraPartenza();
        Time arrivalTime = mission.getOraArrivo();

        if (departureTime != null) {
            departureTimeField.setText(departureTime.toString());
        }

        if (arrivalTime != null) {
            arrivalTimeField.setText(arrivalTime.toString());
        }

        // Calculate and display mission duration
        if (departureTime != null && arrivalTime != null) {
            LocalTime depart = departureTime.toLocalTime();
            LocalTime arrive = arrivalTime.toLocalTime();

            // Calculate duration in minutes
            long durationMinutes = java.time.Duration.between(depart, arrive).toMinutes();

            // Format as hh:mm
            String durationStr = String.format("%02d:%02d", durationMinutes / 60, durationMinutes % 60);
            durationField.setText(durationStr);
        }
    }

    /**
     * Loads flight data from vista_gui_missione view
     */
    private void loadFlightDataFromView(int missionId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DBUtil.getConnection();

            // Query from vista_gui_missione view
            String query = "SELECT GloadMin, GloadMax, QuotaMedia, PosizioniSparo FROM vista_gui_missione " +
                    "WHERE ID_Missione = ?";

            statement = connection.prepareStatement(query);
            statement.setInt(1, missionId);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // Populate flight data fields
                double gloadMin = resultSet.getDouble("GloadMin");
                double gloadMax = resultSet.getDouble("GloadMax");
                int quotaMedia = resultSet.getInt("QuotaMedia");
                String posizioniSparo = resultSet.getString("PosizioniSparo");

                minGLoadField.setText(String.valueOf(gloadMin));
                maxGLoadField.setText(String.valueOf(gloadMax));
                avgAltitudeField.setText(String.valueOf(quotaMedia));

                // If there are fired positions, display them
                if (posizioniSparo != null && !posizioniSparo.isEmpty()) {
                    firedPositionsField.setText(posizioniSparo);
                } else {
                    firedPositionsField.setText("None");
                }
            } else {
                // Clear fields if no data found
                minGLoadField.clear();
                maxGLoadField.clear();
                avgAltitudeField.clear();
                firedPositionsField.setText("None");
            }

        } catch (Exception e) {
            System.err.println("Error loading flight data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Loads weapons (launchers and missiles) data into the table.
     */
    private void loadWeaponsData(int missionId) {
        // Clear existing data
        weaponsList.clear();

        // Load weapons data from database
        List<WeaponStatus> weapons = missionDAO.getWeaponsForMission(missionId);

        if (weapons != null && !weapons.isEmpty()) {
            weaponsList.addAll(weapons);
        }
    }

    /**
     * Handles the Close button click.
     * Closes the mission details window.
     *
     * @param event The action event
     */
    @FXML
    protected void onCloseButtonClick(ActionEvent event) {
        closeWindow();
    }

    /**
     * Handles the Export Report button click.
     * Exports the mission details to a PDF file.
     *
     * @param event The action event
     */
    @FXML
    protected void onExportButtonClick(ActionEvent event) {
        Window owner = exportButton.getScene().getWindow();

        // Show file save dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Mission Report");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        String defaultFileName = "mission_report_" + mission.getId() + ".pdf";
        fileChooser.setInitialFileName(defaultFileName);

        File file = fileChooser.showSaveDialog(owner);
        if (file != null) {
            try {
                // Create a PDF report
                PDFGenerator pdfGenerator = new PDFGenerator();
                pdfGenerator.generateMissionReport(
                        file,
                        mission,
                        weaponsList.toArray(new WeaponStatus[0])
                );

                AlertUtils.showInformation(
                        owner,
                        "Report Exported",
                        "Mission report has been exported successfully to:\n" + file.getAbsolutePath()
                );
            } catch (Exception e) {
                AlertUtils.showError(
                        owner,
                        "Export Error",
                        "Error exporting report: " + e.getMessage()
                );
                e.printStackTrace();
            }
        }
    }

    /**
     * Closes the mission details window.
     */
    private void closeWindow() {
        // Add null checks to prevent the NullPointerException
        if (closeButton != null && closeButton.getScene() != null && closeButton.getScene().getWindow() != null) {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        }
    }
}