package com.aircraft.controller;

import com.aircraft.dao.LauncherDAO;
import com.aircraft.model.Launcher;
import com.aircraft.model.LauncherMission;
import com.aircraft.model.LauncherStatus;
import com.aircraft.util.AlertUtils;
import com.aircraft.util.PDFGenerator;
import com.aircraft.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.util.ArrayList;

import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the Fatigue Monitoring screen.
 * Handles displaying launcher status and generating fatigue monitoring reports.
 */
public class FatigueMonitoringController {

    @FXML
    private Label dateTimeLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label reportTitleLabel;

    @FXML
    private Label maintenanceStatusLabel;

    @FXML
    private ComboBox<String> launcherSerialComboBox;

    @FXML
    private TextField launcherNameField;

    @FXML
    private TextField partNumberField;

    @FXML
    private TextField serialNumberField;

    @FXML
    private TextField missionCountField;

    @FXML
    private TextField firingCountField;

    @FXML
    private TextField flightTimeField;

    @FXML
    private TextField remainingLifeField;

    @FXML
    private VBox graphContainer;

    @FXML
    private TableView<LauncherMission> missionHistoryTable;

    @FXML
    private TableColumn<LauncherMission, Integer> missionIdColumn;

    @FXML
    private TableColumn<LauncherMission, String> missionDateColumn;

    @FXML
    private TableColumn<LauncherMission, String> missionAircraftColumn;

    @FXML
    private TableColumn<LauncherMission, Double> missionFlightTimeColumn;

    @FXML
    private TableColumn<LauncherMission, Double> missionDamageColumn;

    @FXML
    private Button printReportButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button expandGraphButton;

    private final LauncherDAO launcherDAO = new LauncherDAO();
    private LauncherStatus currentLauncherStatus = null;
    private ObservableList<LauncherMission> missionList = FXCollections.observableList(FXCollections.observableArrayList());
    private LineChart<Number, Number> currentChart; // Store the current chart instance

    /**
     * Initializes the controller after its root element has been processed.
     * Sets up event handlers and initializes UI components.
     */
    @FXML
    public void initialize() {
        // Set current date/time and username
        updateDateTime();
        updateUsername();

        // Set up combo box for launcher part numbers (changed from serial numbers)
        loadLauncherPartNumbers();

        // Set up table columns
        setupTableColumns();

        // Set up launcher selection change listener
        launcherSerialComboBox.setOnAction(e -> onLauncherSelected());

        // Set initial UI state
        updateUIState(false);

        // Initially disable expand graph button
        if (expandGraphButton != null) {
            expandGraphButton.setDisable(true);
        }
    }

    /**
     * Updates the date and time label with the current date and time.
     */
    private void updateDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = LocalDateTime.now().format(formatter);
        dateTimeLabel.setText("Date: " + formattedDateTime);
    }

    /**
     * Updates the username label with the current user's username.
     */
    private void updateUsername() {
        // Use getInstance() to get the SessionManager instance
        String username = SessionManager.getInstance().getCurrentUsername();
        usernameLabel.setText("User: " + (username != null ? username : "Unknown"));
    }

    /**
     * Sets up the table columns for the mission history table.
     */
    private void setupTableColumns() {
        // Ensure all columns have the correct PropertyValueFactory names
        missionIdColumn.setCellValueFactory(new PropertyValueFactory<>("missionId"));

        // Explicitly use the getter method name from LauncherMission
        missionDateColumn.setCellValueFactory(new PropertyValueFactory<>("missionDate"));

        missionAircraftColumn.setCellValueFactory(new PropertyValueFactory<>("aircraft"));
        missionFlightTimeColumn.setCellValueFactory(new PropertyValueFactory<>("flightTime"));
        missionDamageColumn.setCellValueFactory(new PropertyValueFactory<>("damageFactor"));

        // Add debug logging to ensure TableView is correctly set up
        System.out.println("Table columns initialized");
    }
    /**
     * Loads launcher part numbers from the database into the combo box.
     * Changed from loadLauncherSerialNumbers()
     */
    private void loadLauncherPartNumbers() {
        List<String> partNumbers = launcherDAO.getAllLauncherPartNumbers();
        launcherSerialComboBox.setItems(FXCollections.observableArrayList(partNumbers));

        // Update the prompt text to reflect the change
        launcherSerialComboBox.setPromptText("Select Part Number");
    }

    /**
     * Handles the selection of a launcher from the combo box.
     */
    /**
     * Updates to FatigueMonitoringController.java
     */

    @FXML
    /**
     * Updated onLauncherSelected method for FatigueMonitoringController
     */
    private void onLauncherSelected() {
        String selectedPartNumber = launcherSerialComboBox.getValue();
        if (selectedPartNumber != null && !selectedPartNumber.isEmpty()) {
            System.out.println("Selected part number: " + selectedPartNumber);

            try {
                // Load launcher status data using part number
                currentLauncherStatus = launcherDAO.getLauncherStatusByPartNumber(selectedPartNumber);

                if (currentLauncherStatus != null) {
                    // Populate form fields
                    populateFormFields(currentLauncherStatus);

                    // Load mission history with better error handling
                    System.out.println("Loading mission history for part number: " + selectedPartNumber);
                    List<LauncherMission> missions = launcherDAO.getMissionHistoryByPartNumber(selectedPartNumber);

                    // Debug output
                    System.out.println("Found " + (missions != null ? missions.size() : 0) + " missions");

                    // Clear and update mission list with UI update check
                    missionList.clear();
                    if (missions != null && !missions.isEmpty()) {
                        missionList.addAll(missions);
                        System.out.println("Added missions to observable list");
                    } else {
                        System.out.println("No missions to add to the table");
                    }

                    // Additional check to force table refresh
                    missionHistoryTable.setItems(null);
                    missionHistoryTable.setItems(missionList);
                    missionHistoryTable.refresh();

                    // Create degradation graph
                    createDegradationGraph(missions != null ? missions : new ArrayList<>());

                    // Update UI state
                    updateUIState(true);

                    // Update report title
                    reportTitleLabel.setText("Fatigue Report: " + selectedPartNumber);

                } else {
                    // Launcher status not found
                    AlertUtils.showError(null, "Data Error", "Launcher status data not found for: " + selectedPartNumber);
                    clearForm();
                    updateUIState(false);
                }
            } catch (Exception e) {
                System.err.println("Error loading launcher data: " + e.getMessage());
                e.printStackTrace();
                AlertUtils.showError(null, "Error", "Failed to load launcher data: " + e.getMessage());
                clearForm();
                updateUIState(false);
            }
        }
    }

    /**
     * Handles the "Refresh" button click.
     * Reloads launcher part numbers from the database.
     *
     * @param event The ActionEvent object
     */
    @FXML
    protected void onRefreshButtonClick(ActionEvent event) {
        loadLauncherPartNumbers();
        updateDateTime();
    }

    /**
     * Handles the "Expand Graph" button click.
     * Opens the degradation graph in a new window.
     *
     * @param event The ActionEvent object
     */
    @FXML
    protected void onExpandGraphButtonClick(ActionEvent event) {
        if (currentChart == null) {
            return;
        }

        // Create a copy of the chart for the popup window
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        xAxis.setLabel("Mission Number");
        yAxis.setLabel("Remaining Life (%)");

        LineChart<Number, Number> popupChart = new LineChart<>(xAxis, yAxis);
        popupChart.setTitle("Launcher Degradation Over Time");
        popupChart.setAnimated(false);
        popupChart.setCreateSymbols(true);

        // Copy the data from the current chart
        for (XYChart.Series<Number, Number> series : currentChart.getData()) {
            XYChart.Series<Number, Number> newSeries = new XYChart.Series<>();
            newSeries.setName(series.getName());

            for (XYChart.Data<Number, Number> data : series.getData()) {
                newSeries.getData().add(new XYChart.Data<>(data.getXValue(), data.getYValue()));
            }

            popupChart.getData().add(newSeries);
        }

        // Create a new stage (window) for the chart
        Stage popupStage = new Stage();
        popupStage.setTitle("Launcher Degradation Chart - " +
                (currentLauncherStatus != null ? currentLauncherStatus.getPartNumber() : ""));
        popupStage.initModality(Modality.NONE); // Non-modal window

        // Make the chart fill the window
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(popupChart);
        borderPane.setPrefSize(800, 600);

        Scene scene = new Scene(borderPane, 800, 600);
        popupStage.setScene(scene);

        // Show the window
        popupStage.show();
    }

    /**
     * Handles the "Print Report" button click.
     * Exports the fatigue monitoring report to a PDF file.
     *
     * @param event The ActionEvent object
     */
    @FXML
    protected void onPrintReportButtonClick(ActionEvent event) {
        Window owner = printReportButton.getScene().getWindow();

        // Validate that a launcher is selected
        if (currentLauncherStatus == null) {
            AlertUtils.showError(owner, "Validation Error", "Please select a launcher first");
            return;
        }

        // Show file save dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Fatigue Monitoring Report");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        String defaultFileName = "fatigue_report_" + currentLauncherStatus.getPartNumber() + ".pdf";
        fileChooser.setInitialFileName(defaultFileName);

        File file = fileChooser.showSaveDialog(owner);
        if (file != null) {
            try {
                // Create a PDF report - use getInstance() to get the SessionManager
                PDFGenerator pdfGenerator = new PDFGenerator();
                pdfGenerator.generateFatigueReport(
                        file,
                        currentLauncherStatus,
                        missionList,
                        SessionManager.getInstance().getCurrentUsername(),
                        maintenanceStatusLabel.getText()
                );

                AlertUtils.showInformation(
                        owner,
                        "Report Exported",
                        "Fatigue monitoring report has been exported successfully to:\n" + file.getAbsolutePath()
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
     * Populates the form fields with launcher status data.
     *
     * @param status The LauncherStatus object with data to populate
     */
    private void populateFormFields(LauncherStatus status) {
        launcherNameField.setText(status.getLauncherName());
        partNumberField.setText(status.getPartNumber());

        // Leave serialNumberField empty or set to N/A as we're now using part numbers
        serialNumberField.setText("N/A");

        missionCountField.setText(String.valueOf(status.getMissionCount()));
        firingCountField.setText(String.valueOf(status.getFiringCount()));

        DecimalFormat df = new DecimalFormat("#,##0.00");
        flightTimeField.setText(df.format(status.getFlightTime()));
        remainingLifeField.setText(df.format(status.getRemainingLifePercentage()));

        // Set maintenance status with color coding
        String maintenanceStatus = status.getMaintenanceStatus();
        maintenanceStatusLabel.setText(maintenanceStatus);

        // Color coding for maintenance status
        switch (maintenanceStatus.toUpperCase()) {
            case "OK":
                maintenanceStatusLabel.setTextFill(Color.GREEN);
                break;
            case "ATTENZIONE":
                maintenanceStatusLabel.setTextFill(Color.ORANGE);
                break;
            case "MANUTENZIONE URGENTE":
                maintenanceStatusLabel.setTextFill(Color.RED);
                break;
            default:
                maintenanceStatusLabel.setTextFill(Color.BLACK);
        }
    }

    /**
     * Creates a graph showing launcher degradation over time.
     *
     * @param missions List of launcher missions to plot
     */
    private void createDegradationGraph(List<LauncherMission> missions) {
        graphContainer.getChildren().clear();

        // Create axes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        xAxis.setLabel("Mission Number");
        yAxis.setLabel("Remaining Life (%)");

        // Create chart
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Launcher Degradation Over Time");
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(true);

        // Create data series
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Remaining Life");

        // If no missions, show 100% remaining life
        if (missions.isEmpty()) {
            series.getData().add(new XYChart.Data<>(1, 100.0));
        } else {
            // Add data points from missions
            double remainingLife = 100.0;

            // Sort missions by date if needed
            // We'll use the missions in reverse chronological order (newest first)
            List<LauncherMission> sortedMissions = new ArrayList<>(missions);
            // No need to sort as the query already sorts them

            for (int i = 0; i < sortedMissions.size(); i++) {
                // For each mission, reduce remaining life by damage factor
                remainingLife -= sortedMissions.get(i).getDamageFactor() * 100;
                remainingLife = Math.max(0, remainingLife); // Don't go below 0

                // Add point to chart (mission number, remaining life)
                series.getData().add(new XYChart.Data<>(i + 1, remainingLife));
            }
        }

        lineChart.getData().add(series);
        graphContainer.getChildren().add(lineChart);

        // Store the current chart
        currentChart = lineChart;

        // Enable/disable expand graph button based on whether there's a chart
        if (expandGraphButton != null) {
            expandGraphButton.setDisable(currentChart == null);
        }
    }

    /**
     * Clears all form fields.
     */
    private void clearForm() {
        launcherNameField.clear();
        partNumberField.clear();
        serialNumberField.clear();
        missionCountField.clear();
        firingCountField.clear();
        flightTimeField.clear();
        remainingLifeField.clear();

        maintenanceStatusLabel.setText("[Status]");
        maintenanceStatusLabel.setTextFill(Color.BLACK);

        missionList.clear();
        graphContainer.getChildren().clear();
        currentChart = null;

        if (expandGraphButton != null) {
            expandGraphButton.setDisable(true);
        }

        reportTitleLabel.setText("Fatigue Report: [Part Number]");
    }

    /**
     * Updates the UI state based on whether a launcher is loaded.
     *
     * @param launcherLoaded true if a launcher is loaded, false otherwise
     */
    private void updateUIState(boolean launcherLoaded) {
        printReportButton.setDisable(!launcherLoaded);
        if (expandGraphButton != null) {
            expandGraphButton.setDisable(!launcherLoaded || currentChart == null);
        }
    }
}