package com.aircraft.model;

/**
 * Model class representing the status of a launcher from vista_stato_vita_lanciatore.
 */
public class LauncherStatus {
    private String launcherName;
    private String partNumber;
    private String serialNumber;
    private int missionCount;
    private int firingCount;
    private int nonFiringCount;
    private double flightTime;
    private double remainingLifePercentage;
    private String maintenanceStatus;

    public LauncherStatus() {
    }

    public LauncherStatus(String launcherName, String partNumber, String serialNumber,
                          int missionCount, int firingCount, int nonFiringCount,
                          double flightTime, double remainingLifePercentage,
                          String maintenanceStatus) {
        this.launcherName = launcherName;
        this.partNumber = partNumber;
        this.serialNumber = serialNumber;
        this.missionCount = missionCount;
        this.firingCount = firingCount;
        this.nonFiringCount = nonFiringCount;
        this.flightTime = flightTime;
        this.remainingLifePercentage = remainingLifePercentage;
        this.maintenanceStatus = maintenanceStatus;
    }

    public String getLauncherName() {
        return launcherName;
    }

    public void setLauncherName(String launcherName) {
        this.launcherName = launcherName;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getMissionCount() {
        return missionCount;
    }

    public void setMissionCount(int missionCount) {
        this.missionCount = missionCount;
    }

    public int getFiringCount() {
        return firingCount;
    }

    public void setFiringCount(int firingCount) {
        this.firingCount = firingCount;
    }

    public int getNonFiringCount() {
        return nonFiringCount;
    }

    public void setNonFiringCount(int nonFiringCount) {
        this.nonFiringCount = nonFiringCount;
    }

    public double getFlightTime() {
        return flightTime;
    }

    public void setFlightTime(double flightTime) {
        this.flightTime = flightTime;
    }

    public double getRemainingLifePercentage() {
        return remainingLifePercentage;
    }

    public void setRemainingLifePercentage(double remainingLifePercentage) {
        this.remainingLifePercentage = remainingLifePercentage;
    }

    public String getMaintenanceStatus() {
        return maintenanceStatus;
    }

    public void setMaintenanceStatus(String maintenanceStatus) {
        this.maintenanceStatus = maintenanceStatus;
    }
}