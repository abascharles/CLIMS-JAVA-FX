package com.aircraft.controller;

import com.aircraft.dao.LauncherDAO;
import com.aircraft.model.Launcher;
import com.aircraft.util.AlertUtils;
import com.aircraft.util.PDFGenerator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * Controller for the Fatigue Monitoring screen.
 * Handles searching for launcher data and generating fatigue monitoring reports.
 */
public class FatigueMonitoringController {

    @FXML
    private TextField launcherSerialField;

    @FXML
    private TextField pylonIdField;

    @FXML
    private TextField aircraftTypeField;

    @FXML
    private TextField flightHoursField;

    @FXML
    private TextField fatigueIndexField;

    @FXML
    private Button searchButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button generateButton;

    @FXML
    private Button printReportButton;

    private final LauncherDAO launcherDAO = new LauncherDAO();
    private Launcher currentLauncher = null;
    private Random random = new Random();

    /**
     * Initializes the controller after its root element has been processed.
     * Sets up event handlers and initializes UI components.
     */
    @FXML
    public void initialize() {
        // Set initial UI state
        updateUIState(false);
    }

    /**
     * Handles the "Search" button click.
     * Searches for a launcher by its serial number.
     *
     * @param event The ActionEvent object
     */
    @FXML
    protected void onSearchButtonClick(ActionEvent event) {
        Window owner = searchButton.getScene().getWindow();

        // Validate input field
        if (launcherSerialField.getText().isEmpty()) {
            AlertUtils.showError(owner, "Search Error", "Please enter a launcher serial number");
            return;
        }

        // Search for launcher
        String partNumber = launcherSerialField.getText();
        currentLauncher = launcherDAO.getByPartNumber(partNumber);

        if (currentLauncher != null) {
            // Launcher found, populate form fields with launcher data and simulated fatigue data
            populateFormFields(currentLauncher);
            updateUIState(true);
        } else {
            // Launcher not found
            AlertUtils.showError(owner, "Search Error", "Launcher not found: " + partNumber);
            clearForm();
            updateUIState(false);
        }
    }

    /**
     * Handles the "Clear" button click.
     * Clears the form fields.
     *
     * @param event The ActionEvent object
     */
    @FXML
    protected void onClearButtonClick(ActionEvent event) {
        clearForm();
        updateUIState(false);
        currentLauncher = null;
    }

    /**
     * Handles the "Generate" button click.
     * Generates a fatigue monitoring report based on the form data.
     *
     * @param event The ActionEvent object
     */
    @FXML
    protected void onGenerateButtonClick(ActionEvent event) {
        Window owner = generateButton.getScene().getWindow();

        // Validate form fields
        if (!validateFormFields()) {
            AlertUtils.showError(owner, "Validation Error", "Please fill in all fields with valid values");
            return;
        }

        // Update fatigue index calculation
        updateFatigueIndex();

        // Show success message
        AlertUtils.showInformation(
                owner,
                "Report Generated",
                "Fatigue monitoring report has been generated successfully.\n" +
                        "Launcher: " + launcherSerialField.getText() + "\n" +
                        "Fatigue Index: " + fatigueIndexField.getText()
        );
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

        // Validate form fields
        if (!validateFormFields()) {
            AlertUtils.showError(owner, "Validation Error", "Please fill in all fields with valid values");
            return;
        }

        // Show file save dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Fatigue Monitoring Report");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        String defaultFileName = "fatigue_report_" + launcherSerialField.getText() + ".pdf";
        fileChooser.setInitialFileName(defaultFileName);

        File file = fileChooser.showSaveDialog(owner);
        if (file != null) {
            try {
                // Create a PDF report
                PDFGenerator pdfGenerator = new PDFGenerator();
                pdfGenerator.generateFatigueReport(
                        file,
                        pylonIdField.getText(),
                        aircraftTypeField.getText(),
                        launcherSerialField.getText(),
                        currentLauncher != null ? currentLauncher.getNomenclatura() : "",
                        Double.parseDouble(flightHoursField.getText()),
                        Double.parseDouble(fatigueIndexField.getText())
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
     * Populates the form fields with launcher data and simulated fatigue data.
     *
     * @param launcher The Launcher object with data to populate
     */
    private void populateFormFields(Launcher launcher) {
        // Generate simulated fatigue monitoring data
        String pylonId = String.format("%05d", 10000 + random.nextInt(90000));
        String aircraftType = getRandomAircraftType();
        double flightHours = 1000 + random.nextInt(4000);

        // Populate form fields
        pylonIdField.setText(pylonId);
        aircraftTypeField.setText(aircraftType);
        flightHoursField.setText(String.valueOf((int)flightHours));

        // Calculate fatigue index based on flight hours and a random factor
        updateFatigueIndex();
    }

    /**
     * Updates the fatigue index based on flight hours and other factors.
     */
    private void updateFatigueIndex() {
        try {
            double flightHours = Double.parseDouble(flightHoursField.getText());

            // Calculate fatigue index (simplified formula for demonstration)
            double baseIndex = flightHours / 10000.0; // Higher hours = higher index
            double randomFactor = 0.8 + (random.nextDouble() * 0.4); // Random factor between 0.8 and 1.2
            double fatigueIndex = baseIndex * randomFactor;

            // Limit to range 0-1 and round to 2 decimal places
            fatigueIndex = Math.min(1.0, fatigueIndex);
            BigDecimal bd = new BigDecimal(fatigueIndex).setScale(2, RoundingMode.HALF_UP);

            fatigueIndexField.setText(bd.toString());
        } catch (NumberFormatException e) {
            // If flight hours is not a valid number, set a default value
            fatigueIndexField.setText("0.00");
        }
    }

    /**
     * Clears all form fields.
     */
    private void clearForm() {
        launcherSerialField.clear();
        pylonIdField.clear();
        aircraftTypeField.clear();
        flightHoursField.clear();
        fatigueIndexField.clear();
    }

    /**
     * Updates the UI state based on whether a launcher is loaded.
     *
     * @param launcherLoaded true if a launcher is loaded, false otherwise
     */
    private void updateUIState(boolean launcherLoaded) {
        generateButton.setDisable(!launcherLoaded);
        printReportButton.setDisable(!launcherLoaded);

        // Set fields editable/readonly based on state
        pylonIdField.setEditable(launcherLoaded);
        aircraftTypeField.setEditable(launcherLoaded);
        flightHoursField.setEditable(launcherLoaded);
        fatigueIndexField.setEditable(false); // Always calculated automatically
    }

    /**
     * Validates the form fields.
     *
     * @return true if all fields are valid, false otherwise
     */
    private boolean validateFormFields() {
        if (pylonIdField.getText().isEmpty() ||
                aircraftTypeField.getText().isEmpty() ||
                flightHoursField.getText().isEmpty() ||
                fatigueIndexField.getText().isEmpty()) {
            return false;
        }

        try {
            // Validate flight hours field
            double flightHours = Double.parseDouble(flightHoursField.getText());
            if (flightHours < 0) {
                return false;
            }

            // Validate fatigue index field
            double fatigueIndex = Double.parseDouble(fatigueIndexField.getText());
            return !(fatigueIndex < 0 || fatigueIndex > 1);

        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns a random aircraft type for demonstration purposes.
     *
     * @return A random aircraft type
     */
    private String getRandomAircraftType() {
        String[] aircraftTypes = {
                "Typhoon", "F-35", "F-16", "F/A-18", "Rafale",
                "Eurofighter", "Su-35", "MiG-29", "Gripen", "F-22"
        };
        return aircraftTypes[random.nextInt(aircraftTypes.length)];
    }
}