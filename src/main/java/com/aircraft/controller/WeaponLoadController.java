package com.aircraft.controller;

import com.aircraft.dao.WeaponDAO;
import com.aircraft.model.Weapon;
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
 * Controller for the Weapon Load module.
 * Handles adding new weapons and viewing the weapon list.
 */
public class WeaponLoadController {
    @FXML
    private TextField partNumberField;

    @FXML
    private TextField nomenclaturaField;

    @FXML
    private TextField codiceDittaField;

    @FXML
    private TextField massaField;

    @FXML
    private Button saveButton;

    @FXML
    private Button homeButton;

    @FXML
    private TableView<Weapon> weaponTableView;

    @FXML
    private TableColumn<Weapon, String> partNumberColumn;

    @FXML
    private TableColumn<Weapon, String> nomenclaturaColumn;

    @FXML
    private TableColumn<Weapon, String> codiceDittaColumn;

    @FXML
    private TableColumn<Weapon, BigDecimal> massaColumn;

    @FXML
    private VBox formPane;

    @FXML
    private VBox listPane;

    @FXML
    private VBox mainPane;

    @FXML
    private VBox mainScreen;

    private final WeaponDAO weaponDAO = new WeaponDAO();
    private ObservableList<Weapon> weaponList = FXCollections.observableArrayList();

    /**
     * Initializes the controller after its root element has been processed.
     * Sets up event handlers and initializes UI components.
     */
    @FXML
    public void initialize() {
        // Set up table columns
        partNumberColumn.setCellValueFactory(new PropertyValueFactory<>("partNumber"));
        nomenclaturaColumn.setCellValueFactory(new PropertyValueFactory<>("nomenclatura"));
        codiceDittaColumn.setCellValueFactory(new PropertyValueFactory<>("codiceDitta"));
        massaColumn.setCellValueFactory(new PropertyValueFactory<>("massa"));

        // Load weapon data
        refreshWeaponTable();

        // Initially show only the main view
        showMainView();
    }

    /**
     * Refreshes the weapon table with data from the database.
     */
    private void refreshWeaponTable() {
        weaponList.clear();
        weaponList.addAll(weaponDAO.getAll());
        weaponTableView.setItems(weaponList);
    }

    /**
     * Shows the main view with aircraft image and buttons.
     */
    private void showMainView() {
        mainScreen.setVisible(true);
        mainScreen.setManaged(true);
        formPane.setVisible(false);
        formPane.setManaged(false);
        listPane.setVisible(false);
        listPane.setManaged(false);
    }

    /**
     * Shows the form view for adding a new weapon.
     */
    private void showFormView() {
        mainScreen.setVisible(false);
        mainScreen.setManaged(false);
        formPane.setVisible(true);
        formPane.setManaged(true);
        listPane.setVisible(false);
        listPane.setManaged(false);
    }

    /**
     * Shows the list view for viewing existing weapons.
     */
    private void showListView() {
        mainScreen.setVisible(false);
        mainScreen.setManaged(false);
        formPane.setVisible(false);
        formPane.setManaged(false);
        listPane.setVisible(true);
        listPane.setManaged(true);

        // Refresh the data when showing the list
        refreshWeaponTable();
    }

    /**
     * Handles the "Insert New Data" button click.
     *
     * @param event The ActionEvent object
     */
    @FXML
    protected void onInsertNewDataClick(ActionEvent event) {
        // Clear the form fields
        clearForm();

        // Show the form view
        showFormView();
    }

    /**
     * Handles the "View Weapon List" button click.
     *
     * @param event The ActionEvent object
     */
    @FXML
    protected void onViewWeaponListClick(ActionEvent event) {
        // Show the list view
        showListView();
    }

    /**
     * Handles the "Save" button click in the form.
     * Validates and saves the weapon data to the database.
     *
     * @param event The ActionEvent object
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
            AlertUtils.showError(owner, "Validation Error", "Nomenclature is required");
            return;
        }

        if (codiceDittaField.getText().isEmpty()) {
            AlertUtils.showError(owner, "Validation Error", "Company Code is required");
            return;
        }

        if (massaField.getText().isEmpty()) {
            AlertUtils.showError(owner, "Validation Error", "Mass is required");
            return;
        }

        // Parse mass value
        BigDecimal massa;
        try {
            massa = new BigDecimal(massaField.getText().replace(',', '.'));
        } catch (NumberFormatException e) {
            AlertUtils.showError(owner, "Validation Error", "Mass must be a valid number");
            return;
        }

        // Check if weapon already exists
        if (weaponDAO.exists(partNumberField.getText())) {
            AlertUtils.showError(owner, "Validation Error", "Weapon with this Part Number already exists");
            return;
        }

        // Create weapon object
        Weapon weapon = new Weapon();
        weapon.setPartNumber(partNumberField.getText());
        weapon.setNomenclatura(nomenclaturaField.getText());
        weapon.setCodiceDitta(codiceDittaField.getText());
        weapon.setMassa(massa);

        // Save weapon
        boolean success = weaponDAO.insert(weapon);

        if (success) {
            AlertUtils.showInformation(owner, "Success", "Data entered correctly");
            clearForm();
            showMainView(); // Return to main view after successful save
        } else {
            AlertUtils.showError(owner, "Error", "Failed to save weapon data");
        }
    }

    /**
     * Handles the "Home" button click.
     * Returns to the main view.
     *
     * @param event The ActionEvent object
     */
    @FXML
    protected void onHomeButtonClick(ActionEvent event) {
        showMainView();
    }

    /**
     * Clears all form fields.
     */
    private void clearForm() {
        partNumberField.clear();
        nomenclaturaField.clear();
        codiceDittaField.clear();
        massaField.clear();
    }
}