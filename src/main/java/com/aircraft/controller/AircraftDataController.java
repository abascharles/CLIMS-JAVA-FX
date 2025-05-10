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
 import javafx.scene.layout.VBox;
 import javafx.stage.Window;

 import java.util.List;

 /**
  * Controller for the Aircraft Data management screen.
  * Handles creating, updating, and deleting aircraft records.
  */
 public class AircraftDataController {

     @FXML private VBox mainScreen;
     @FXML private VBox formScreen;
     @FXML private VBox listScreen;

     @FXML private TextField registrationField;
     @FXML private Button saveButton;

     @FXML private TableView<Aircraft> aircraftTable;
     @FXML private TableColumn<Aircraft, String> registrationColumn;

     private final AircraftDAO aircraftDAO = new AircraftDAO();
     private ObservableList<Aircraft> aircraftList = FXCollections.observableArrayList();
     private Aircraft selectedAircraft = null;

     /**
      * Initializes the controller after its root element has been processed.
      */
     @FXML
     public void initialize() {
         // Initialize table columns
         registrationColumn.setCellValueFactory(new PropertyValueFactory<>("matricolaVelivolo"));

         // Load data
         refreshAircraftTable();
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
         selectedAircraft = null;
     }

     /**
      * Handles navigation to the aircraft list screen.
      */
     @FXML
     protected void onViewAircraftListClick(ActionEvent event) {
         mainScreen.setVisible(false);
         mainScreen.setManaged(false);
         formScreen.setVisible(false);
         formScreen.setManaged(false);
         listScreen.setVisible(true);
         listScreen.setManaged(true);

         refreshAircraftTable();
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
      */
     @FXML
     protected void onSaveButtonClick(ActionEvent event) {
         Window owner = saveButton.getScene().getWindow();

         // Validate input fields
         if (registrationField.getText().isEmpty()) {
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

         aircraft.setMatricolaVelivolo(registrationField.getText());

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

             // Return to main screen after saving
             onBackButtonClick(event);
         } else {
             AlertUtils.showError(owner, "Error", "Failed to save aircraft");
         }
     }

     /**
      * Handles the "Clear" button click.
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
         registrationField.clear();
     }

     /**
      * Handles the "Edit" button click for a selected aircraft.
      */
     @FXML
     protected void onEditButtonClick(ActionEvent event) {
         Aircraft aircraft = aircraftTable.getSelectionModel().getSelectedItem();
         if (aircraft != null) {
             selectedAircraft = aircraft;

             // Populate form fields
             registrationField.setText(aircraft.getMatricolaVelivolo());

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
      * Handles the "Delete" button click for a selected aircraft.
      */
     @FXML
     protected void onDeleteButtonClick(ActionEvent event) {
         Aircraft aircraft = aircraftTable.getSelectionModel().getSelectedItem();
         if (aircraft != null) {
             Window owner = aircraftTable.getScene().getWindow();

             boolean confirmed = AlertUtils.showConfirmation(
                     owner,
                     "Confirm Deletion",
                     "Are you sure you want to delete aircraft: " + aircraft.getMatricolaVelivolo() + "?"
             );

             if (confirmed) {
                 boolean success = aircraftDAO.delete(aircraft.getMatricolaVelivolo());

                 if (success) {
                     AlertUtils.showInformation(owner, "Success", "Aircraft deleted successfully");
                     refreshAircraftTable();
                 } else {
                     AlertUtils.showError(owner, "Error", "Failed to delete aircraft");
                 }
             }
         }
     }
 }