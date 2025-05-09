package com.aircraft.model;

/**
 * Model class representing the status of a weapon (launcher and missile) for a specific mission.
 */
public class WeaponStatus {
    private String position;
    private String launcherPartNumber;
    private String launcherSerialNumber;
    private String missilePartNumber;
    private String missileName;
    private String status; // A_BORDO, SPARATO, SBARCATO

    public WeaponStatus() {
    }

    public WeaponStatus(String position, String launcherPartNumber, String launcherSerialNumber,
                        String missilePartNumber, String missileName, String status) {
        this.position = position;
        this.launcherPartNumber = launcherPartNumber;
        this.launcherSerialNumber = launcherSerialNumber;
        this.missilePartNumber = missilePartNumber;
        this.missileName = missileName;
        this.status = status;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getLauncherPartNumber() {
        return launcherPartNumber;
    }

    public void setLauncherPartNumber(String launcherPartNumber) {
        this.launcherPartNumber = launcherPartNumber;
    }

    public String getLauncherSerialNumber() {
        return launcherSerialNumber;
    }

    public void setLauncherSerialNumber(String launcherSerialNumber) {
        this.launcherSerialNumber = launcherSerialNumber;
    }

    public String getMissilePartNumber() {
        return missilePartNumber;
    }

    public void setMissilePartNumber(String missilePartNumber) {
        this.missilePartNumber = missilePartNumber;
    }

    public String getMissileName() {
        return missileName;
    }

    public void setMissileName(String missileName) {
        this.missileName = missileName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}