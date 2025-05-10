package com.aircraft.controller;

import com.aircraft.dao.LauncherDAO;
import com.aircraft.model.Launcher;
import com.aircraft.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.math.BigDecimal;

/**
 * Controller for the Launcher management screen.
 */
public class LauncherController {

    @FXML
    private VBox mainScreen;

    @FXML
    private VBox formScreen;

    @FXML
    private VBox listScreen;

    @FXML
    private TextField partNumberField;

    @FXML
    private TextField nomenclaturaField;

    @FXML
    private TextField codiceDittaField;

    @FXML
    private TextField oreVitaOperativaField;

    @FXML
    private Button saveButton;

    @FXML
    private TableView<Launcher> launcherTable;

    @FXML
    private TableColumn<Launcher, String> partNumberColumn;

    @FXML
    private TableColumn<Launcher, String> nomenclaturaColumn;

    @FXML
    private TableColumn<Launcher, String> codiceDittaColumn;

    @FXML
    private TableColumn<Launcher, Number> oreVitaOperativaColumn;

    private final LauncherDAO launcherDAO = new LauncherDAO();
    private ObservableList<Launcher> launcherList = FXCollections.observableArrayList();
    private Launcher selectedLauncher = null;

    /**
     * Initializes the controller after its root element has been processed.
     */
    @FXML
    public void initialize() {
        // Initialize table columns
        partNumberColumn.setCellValueFactory(new PropertyValueFactory<>("partNumber"));
        nomenclaturaColumn.setCellValueFactory(new PropertyValueFactory<>("nomenclatura"));
        codiceDittaColumn.setCellValueFactory(new PropertyValueFactory<>("codiceDitta"));
        oreVitaOperativaColumn.setCellValueFactory(new PropertyValueFactory<>("oreVitaOperativa"));

        // Load data
        refreshLauncherTable();
    }

    /**
     * Handles navigation to the insert new data screen.
     */
    @FXML
    protected void onInsertNewDataClick(ActionEvent event) {
        mainScreen.setVisible(false);
        mainScreen.setManaged(false);
        formScreen.setVisible(true);
        formScreen.setManaged(true);
        listScreen.setVisible(false);
        listScreen.setManaged(false);

        clearForm();
        selectedLauncher = null;
    }

    /**
     * Handles navigation to the launcher list screen.
     */
    @FXML
    protected void onViewLauncherListClick(ActionEvent event) {
        mainScreen.setVisible(false);
        mainScreen.setManaged(false);
        formScreen.setVisible(false);
        formScreen.setManaged(false);
        listScreen.setVisible(true);
        listScreen.setManaged(true);

        refreshLauncherTable();
    }

    /**
     * Handles navigation back to the main screen.
     */
    @FXML
    protected void onBackButtonClick(ActionEvent event) {
        mainScreen.setVisible(true);
        mainScreen.setManaged(true);
        formScreen.setVisible(false);
        formScreen.setManaged(false);
        listScreen.setVisible(false);
        listScreen.setManaged(false);
    }

    /**
     * Refreshes the launcher table with data from the database.
     */
    private void refreshLauncherTable() {
        launcherList.clear();
        launcherList.addAll(launcherDAO.getAll());
        launcherTable.setItems(launcherList);
    }

    /**
     * Handles the "Save" button click.
     */
    @FXML
    protected void onSaveButtonClick(ActionEvent event) {
        Window owner = saveButton.getScene().getWindow();

        // Validate input fields
        if (partNumberField.getText().isEmpty()) {
            AlertUtils.showError(owner, "Validation Error", "Part Number is required");
            return;
        }

        if (nomenclaturaField.getText().isEmpty()) {
            AlertUtils.showError(owner, "Validation Error", "Nomenclatura is required");
            return;
        }

        // Create or update launcher object
        Launcher launcher;
        if (selectedLauncher == null) {
            // Create new launcher
            launcher = new Launcher();
        } else {
            // Update existing launcher
            launcher = selectedLauncher;
        }

        launcher.setPartNumber(partNumberField.getText());
        launcher.setNomenclatura(nomenclaturaField.getText());
        launcher.setCodiceDitta(codiceDittaField.getText());

        try {
            double oreVitaOperativa = Double.parseDouble(oreVitaOperativaField.getText());
            launcher.setOreVitaOperativa(BigDecimal.valueOf(oreVitaOperativa));
        } catch (NumberFormatException e) {
            AlertUtils.showError(owner, "Validation Error", "Ore Vita Operativa must be a valid number");
            return;
        }

        // Save launcher
        boolean success;
        if (selectedLauncher == null) {
            success = launcherDAO.insert(launcher);
        } else {
            success = launcherDAO.update(launcher);
        }

        if (success) {
            AlertUtils.showInformation(owner, "Success", "Launcher saved successfully");
            clearForm();
            selectedLauncher = null;
            refreshLauncherTable();

            // Return to main screen after saving
            onBackButtonClick(event);
        } else {
            AlertUtils.showError(owner, "Error", "Failed to save launcher");
        }
    }

    /**
     * Handles the "Clear" button click.
     */
    @FXML
    protected void onClearButtonClick(ActionEvent event) {
        clearForm();
        selectedLauncher = null;
    }

    /**
     * Clears all form fields.
     */
    private void clearForm() {
        partNumberField.clear();
        nomenclaturaField.clear();
        codiceDittaField.clear();
        oreVitaOperativaField.clear();
    }

    /**
     * Handles the "Edit" button click for a selected launcher.
     */
    @FXML
    protected void onEditButtonClick(ActionEvent event) {
        Launcher launcher = launcherTable.getSelectionModel().getSelectedItem();
        if (launcher != null) {
            selectedLauncher = launcher;

            // Populate form fields
            partNumberField.setText(launcher.getPartNumber());
            nomenclaturaField.setText(launcher.getNomenclatura());
            codiceDittaField.setText(launcher.getCodiceDitta());
            oreVitaOperativaField.setText(String.valueOf(launcher.getOreVitaOperativa()));

            // Switch to form screen for editing
            mainScreen.setVisible(false);
            mainScreen.setManaged(false);
            formScreen.setVisible(true);
            formScreen.setManaged(true);
            listScreen.setVisible(false);
            listScreen.setManaged(false);
        }
    }

    /**
     * Handles the "Delete" button click for a selected launcher.
     */
    @FXML
    protected void onDeleteButtonClick(ActionEvent event) {
        Launcher launcher = launcherTable.getSelectionModel().getSelectedItem();
        if (launcher != null) {
            Window owner = launcherTable.getScene().getWindow();

            // Confirm deletion
            boolean confirmed = AlertUtils.showConfirmation(
                    owner,
                    "Confirm Deletion",
                    "Are you sure you want to delete launcher: " + launcher.getPartNumber() + "?"
            );

            if (confirmed) {
                // Delete launcher
                boolean success = launcherDAO.delete(launcher.getPartNumber());

                if (success) {
                    AlertUtils.showInformation(owner, "Success", "Launcher deleted successfully");
                    refreshLauncherTable();
                } else {
                    AlertUtils.showError(owner, "Error", "Failed to delete launcher");
                }
            }
        }
    }
}