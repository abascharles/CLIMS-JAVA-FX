package com.aircraft.controller;

import com.aircraft.dao.AircraftDAO;
import com.aircraft.dao.MissionDAO;
import com.aircraft.model.Aircraft;
import com.aircraft.model.Mission;
import com.aircraft.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for the Mission History screen.
 * Allows searching and viewing historical mission data.
 */
public class MissionHistoryController {

    @FXML
    private ComboBox<Aircraft> aircraftComboBox;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private Button searchButton;

    @FXML
    private Button exportButton;

    @FXML
    private TableView<Mission> missionTable;

    @FXML
    private TableColumn<Mission, Integer> idColumn;

    @FXML
    private TableColumn<Mission, String> aircraftColumn;

    @FXML
    private TableColumn<Mission, Integer> flightNumberColumn;

    @FXML
    private TableColumn<Mission, Date> missionDateColumn;

    @FXML
    private TableColumn<Mission, Time> departureTimeColumn;

    @FXML
    private TableColumn<Mission, Time> arrivalTimeColumn;

    @FXML
    private TableColumn<Mission, Void> detailsColumn;

    private final MissionDAO missionDAO = new MissionDAO();
    private final AircraftDAO aircraftDAO = new AircraftDAO();
    private ObservableList<Mission> missionList = FXCollections.observableArrayList();
    private ObservableList<Aircraft> aircraftList = FXCollections.observableArrayList();

    /**
     * Initializes the controller after its root element has been processed.
     * Sets up event handlers and initializes UI components.
     */
    @FXML
    public void initialize() {
        // Load aircraft data for the combo box
        loadAircraftData();

        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        aircraftColumn.setCellValueFactory(new PropertyValueFactory<>("matricolaVelivolo"));
        flightNumberColumn.setCellValueFactory(new PropertyValueFactory<>("numeroVolo"));
        missionDateColumn.setCellValueFactory(new PropertyValueFactory<>("dataMissione"));
        departureTimeColumn.setCellValueFactory(new PropertyValueFactory<>("oraPartenza"));
        arrivalTimeColumn.setCellValueFactory(new PropertyValueFactory<>("oraArrivo"));

        // Set up details column with View Details button
        setupDetailsColumn();

        // Set default date range to last 30 days
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        fromDatePicker.setValue(thirtyDaysAgo);
        toDatePicker.setValue(today);

        // Initially load all missions from the last 30 days
        searchMissions();
    }

    /**
     * Loads aircraft data for the combo box.
     */
    private void loadAircraftData() {
        List<Aircraft> aircraft = aircraftDAO.getAll();
        aircraftList.clear();

        // Add an "All Aircraft" option
        Aircraft allAircraft = new Aircraft();
        allAircraft.setMatricolaVelivolo("All Aircraft");
        aircraftList.add(allAircraft);

        // Add actual aircraft
        aircraftList.addAll(aircraft);
        aircraftComboBox.setItems(aircraftList);

        // Set default selection to "All Aircraft"
        aircraftComboBox.getSelectionModel().selectFirst();

        // Set up cell factory to display aircraft matricola
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

        // Set up button cell to display selected aircraft matricola
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
    }

    /**
     * Sets up the details column with a View Details button.
     */
    private void setupDetailsColumn() {
        detailsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View Details");

            {
                // Set up button handler
                viewButton.setOnAction(event -> {
                    Mission mission = getTableView().getItems().get(getIndex());
                    viewMissionDetails(mission);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });
    }

    /**
     * Shows details for a selected mission.
     *
     * @param mission The Mission object to view details for
     */
    private void viewMissionDetails(Mission mission) {
        try {
            // Load the mission details FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mission_details.fxml"));
            Parent root = loader.load();

            // Get the controller
            MissionDetailsController controller = loader.getController();

            // Set the mission to load data
            controller.setMission(mission.getId());

            // Create a new stage (window) for the details view
            Stage detailsStage = new Stage();
            detailsStage.setTitle("Mission Details - #" + mission.getId());
            detailsStage.initModality(Modality.WINDOW_MODAL); // Modal dialog
            detailsStage.initOwner(missionTable.getScene().getWindow()); // Set parent window

            Scene scene = new Scene(root);
            detailsStage.setScene(scene);

            // Show the window
            detailsStage.showAndWait();
        } catch (IOException e) {
            // Show error alert if there's a problem loading the view
            AlertUtils.showError(
                    missionTable.getScene().getWindow(),
                    "Error Opening Details",
                    "Error opening mission details: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    /**
     * Searches for missions based on filter criteria.
     */
    private void searchMissions() {
        // Get filter criteria
        Aircraft selectedAircraft = aircraftComboBox.getValue();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        // Validate date range
        if (fromDate == null || toDate == null) {
            AlertUtils.showError(
                    searchButton.getScene().getWindow(),
                    "Validation Error",
                    "Please select both From and To dates"
            );
            return;
        }

        if (fromDate.isAfter(toDate)) {
            AlertUtils.showError(
                    searchButton.getScene().getWindow(),
                    "Validation Error",
                    "From date must be before or equal to To date"
            );
            return;
        }

        // Convert dates to SQL Date
        Date sqlFromDate = Date.valueOf(fromDate);
        Date sqlToDate = Date.valueOf(toDate);

        // Search for missions
        List<Mission> missions;
        if (selectedAircraft != null && !selectedAircraft.getMatricolaVelivolo().equals("All Aircraft")) {
            // Search by aircraft and date range
            missions = missionDAO.getMissionsByAircraftAndDateRange(
                    selectedAircraft.getMatricolaVelivolo(),
                    sqlFromDate,
                    sqlToDate
            );
        } else {
            // Search by date range only
            missions = missionDAO.getMissionsByDateRange(sqlFromDate, sqlToDate);
        }

        // Update table
        missionList.clear();
        missionList.addAll(missions);
        missionTable.setItems(missionList);

        // Enable/disable export button based on results
        exportButton.setDisable(missions.isEmpty());
    }

    /**
     * Handles the "Search" button click.
     * Searches for missions based on filter criteria.
     *
     * @param event The ActionEvent object
     */
    @FXML
    protected void onSearchButtonClick(ActionEvent event) {
        searchMissions();
    }

    /**
     * Handles the "Clear" button click.
     * Clears the filter criteria.
     *
     * @param event The ActionEvent object
     */
    @FXML
    protected void onClearButtonClick(ActionEvent event) {
        // Reset filter criteria
        aircraftComboBox.getSelectionModel().selectFirst(); // Select "All Aircraft"

        // Reset date range to last 30 days
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        fromDatePicker.setValue(thirtyDaysAgo);
        toDatePicker.setValue(today);

        // Search with reset criteria
        searchMissions();
    }

    /**
     * Handles the "Export Results" button click.
     * Exports the mission results to a CSV file.
     *
     * @param event The ActionEvent object
     */
    @FXML
    protected void onExportButtonClick(ActionEvent event) {
        if (missionList.isEmpty()) {
            AlertUtils.showError(
                    exportButton.getScene().getWindow(),
                    "Export Error",
                    "No data to export"
            );
            return;
        }

        // Show file save dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Mission Data");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("mission_data.csv");

        File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());
        if (file != null) {
            exportToCSV(file);
        }
    }

    /**
     * Exports mission data to a CSV file.
     *
     * @param file The file to export data to
     */
    private void exportToCSV(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            // Write CSV header
            writer.write("ID,Aircraft,Flight Number,Date,Departure Time,Arrival Time\n");

            // Write data rows
            for (Mission mission : missionList) {
                writer.write(String.format("%d,%s,%d,%s,%s,%s\n",
                        mission.getId(),
                        mission.getMatricolaVelivolo(),
                        mission.getNumeroVolo(),
                        mission.getDataMissione(),
                        mission.getOraPartenza(),
                        mission.getOraArrivo()
                ));
            }

            AlertUtils.showInformation(
                    exportButton.getScene().getWindow(),
                    "Export Successful",
                    "Mission data exported successfully to:\n" + file.getAbsolutePath()
            );
        } catch (IOException e) {
            AlertUtils.showError(
                    exportButton.getScene().getWindow(),
                    "Export Error",
                    "Error exporting data: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }
}