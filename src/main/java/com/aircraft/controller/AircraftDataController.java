package com.aircraft.controller;

import com.aircraft.dao.AircraftDAO;
import com.aircraft.model.Aircraft;
import com.aircraft.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Window;

import java.util.List;

/**
 * Controller for the Aircraft Data management screen.
 * Handles creating, updating, and deleting aircraft records.
 */
public class AircraftDataController {

    @FXML
    private TextField matricolaVelivoloField;

    @FXML
    private Button saveButton;

    @FXML
    private TableView<Aircraft> aircraftTable;

    @FXML
    private TableColumn<Aircraft, String> matricolaVelivoloColumn;

    @FXML
    private TableColumn<Aircraft, Void> actionsColumn;

    private final AircraftDAO aircraftDAO = new AircraftDAO();
    private ObservableList<Aircraft> aircraftList = FXCollections.observableArrayList();
    private Aircraft selectedAircraft = null;

    /**
     * Initializes the controller after its root element has been processed.
     * Sets up event handlers and initializes UI components.
     */
    @FXML
    public void initialize() {
        // Set up table columns
        matricolaVelivoloColumn.setCellValueFactory(new PropertyValueFactory<>("matricolaVelivolo"));

        // Set up action column with Edit and Delete buttons
        setupActionsColumn();

        // Load aircraft data
        refreshAircraftTable();
    }

    /**
     * Sets up the actions column with Edit and Delete buttons.
     */
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                // Set up button handlers
                editButton.setOnAction(event -> {
                    Aircraft aircraft = getTableView().getItems().get(getIndex());
                    editAircraft(aircraft);
                });

                deleteButton.setOnAction(event -> {
                    Aircraft aircraft = getTableView().getItems().get(getIndex());
                    deleteAircraft(aircraft);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
    }

    /**
     * Loads an aircraft for editing.
     *
     * @param aircraft The Aircraft object to edit
     */
    private void editAircraft(Aircraft aircraft) {
        // Set selected aircraft
        selectedAircraft = aircraft;

        // Populate form fields
        matricolaVelivoloField.setText(aircraft.getMatricolaVelivolo());
    }

    /**
     * Deletes an aircraft after confirmation.
     *
     * @param aircraft The Aircraft object to delete
     */
    private void deleteAircraft(Aircraft aircraft) {
        Window owner = aircraftTable.getScene().getWindow();

        // Confirm deletion
        boolean confirmed = AlertUtils.showConfirmation(
                owner,
                "Confirm Deletion",
                "Are you sure you want to delete aircraft: " + aircraft.getMatricolaVelivolo() + "?"
        );

        if (confirmed) {
            // Delete aircraft
            boolean success = aircraftDAO.delete(aircraft.getMatricolaVelivolo());

            if (success) {
                AlertUtils.showInformation(owner, "Success", "Aircraft deleted successfully");
                refreshAircraftTable();
            } else {
                AlertUtils.showError(owner, "Error", "Failed to delete aircraft");
            }
        }
    }

    /**
     * Refreshes the aircraft table with data from the database.
     */
    private void refreshAircraftTable() {
        List<Aircraft> aircraft = aircraftDAO.getAll();
        aircraftList.clear();
        aircraftList.addAll(aircraft);
        aircraftTable.setItems(aircraftList);
    }

    /**
     * Handles the "Save" button click.
     * Validates and saves the aircraft data to the database.
     *
     * @param event The ActionEvent object
     */
    @FXML
    protected void onSaveButtonClick(ActionEvent event) {
        Window owner = saveButton.getScene().getWindow();

        // Validate input fields
        if (matricolaVelivoloField.getText().isEmpty()) {
            AlertUtils.showError(owner, "Validation Error", "Matricola Velivolo is required");
            return;
        }

        // Create or update aircraft object
        Aircraft aircraft;
        if (selectedAircraft == null) {
            // Create new aircraft
            aircraft = new Aircraft();
        } else {
            // Update existing aircraft
            aircraft = selectedAircraft;
        }

        aircraft.setMatricolaVelivolo(matricolaVelivoloField.getText());

        // Save aircraft
        boolean success;
        if (selectedAircraft == null) {
            // Check if aircraft already exists
            if (aircraftDAO.exists(aircraft.getMatricolaVelivolo())) {
                AlertUtils.showError(owner, "Validation Error", "Aircraft with this Matricola already exists");
                return;
            }

            success = aircraftDAO.insert(aircraft);
        } else {
            success = aircraftDAO.update(aircraft);
        }

        if (success) {
            AlertUtils.showInformation(owner, "Success", "Aircraft saved successfully");
            clearForm();
            selectedAircraft = null;
            refreshAircraftTable();
        } else {
            AlertUtils.showError(owner, "Error", "Failed to save aircraft");
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
        selectedAircraft = null;
    }

    /**
     * Clears all form fields.
     */
    private void clearForm() {
        matricolaVelivoloField.clear();
    }
}