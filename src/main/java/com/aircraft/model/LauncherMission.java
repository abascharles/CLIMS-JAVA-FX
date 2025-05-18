package com.aircraft.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing a mission record for a launcher.
 */
public class LauncherMission {
    private int missionId;
    private LocalDate missionDate;
    private String aircraft;
    private double flightTime;
    private double damageFactor;
    private String launcherSerialNumber; // Keep for backward compatibility
    private String launcherPartNumber;   // Add this field

    // Add getters and setters for launcherPartNumber
    public String getLauncherPartNumber() {
        return launcherPartNumber;
    }

    public void setLauncherPartNumber(String launcherPartNumber) {
        this.launcherPartNumber = launcherPartNumber;
    }

    public LauncherMission() {
    }

    public LauncherMission(int missionId, LocalDate missionDate, String aircraft,
                           double flightTime, double damageFactor, String launcherSerialNumber) {
        this.missionId = missionId;
        this.missionDate = missionDate;
        this.aircraft = aircraft;
        this.flightTime = flightTime;
        this.damageFactor = damageFactor;
        this.launcherSerialNumber = launcherSerialNumber;
    }

    public int getMissionId() {
        return missionId;
    }

    public void setMissionId(int missionId) {
        this.missionId = missionId;
    }

    public LocalDate getMissionDateObj() {
        return missionDate;
    }

    public void setMissionDate(LocalDate missionDate) {
        this.missionDate = missionDate;
    }

    /**
     * Get formatted mission date as string for display
     */
    public String getMissionDate() {
        if (missionDate == null) {
            return "";
        }
        return missionDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public String getAircraft() {
        return aircraft;
    }

    public void setAircraft(String aircraft) {
        this.aircraft = aircraft;
    }

    public double getFlightTime() {
        return flightTime;
    }

    public void setFlightTime(double flightTime) {
        this.flightTime = flightTime;
    }

    public double getDamageFactor() {
        return damageFactor;
    }

    public void setDamageFactor(double damageFactor) {
        this.damageFactor = damageFactor;
    }

    public String getLauncherSerialNumber() {
        return launcherSerialNumber;
    }

    public void setLauncherSerialNumber(String launcherSerialNumber) {
        this.launcherSerialNumber = launcherSerialNumber;
    }
}