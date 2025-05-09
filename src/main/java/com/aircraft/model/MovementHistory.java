package com.aircraft.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing a movement history record for launchers or missiles.
 */
public class MovementHistory {
    private String partNumber;
    private String itemType; // "Launcher" or "Missile"
    private String itemName; // Nomenclatura
    private String serialNumber; // Only for launchers
    private LocalDate date;
    private String actionType; // "Embarkation" or "Disembarkation"
    private String location; // PosizioneVelivolo
    private String aircraftId; // MatricolaVelivolo (may be null for some records)

    public MovementHistory() {
    }

    public MovementHistory(String partNumber, String itemType, String itemName, String serialNumber,
                           LocalDate date, String actionType, String location, String aircraftId) {
        this.partNumber = partNumber;
        this.itemType = itemType;
        this.itemName = itemName;
        this.serialNumber = serialNumber;
        this.date = date;
        this.actionType = actionType;
        this.location = location;
        this.aircraftId = aircraftId;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * Returns a formatted string representation of the date.
     * This is used by the TableView through PropertyValueFactory.
     *
     * @return A formatted date string
     */
    public String getFormattedDate() {
        if (date == null) {
            return "";
        }
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAircraftId() {
        return aircraftId;
    }

    public void setAircraftId(String aircraftId) {
        this.aircraftId = aircraftId;
    }
}