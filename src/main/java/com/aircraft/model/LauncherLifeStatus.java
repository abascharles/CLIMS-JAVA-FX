package com.aircraft.model;

import java.math.BigDecimal;

/**
 * Model class representing launcher life status information.
 * Corresponds to the 'vista_stato_vita_lanciatore' view in the database.
 */
public class LauncherLifeStatus {
    private String nomeLanciatore;
    private String partNumber;
    private String serialNumber;
    private int numeroMissioni;
    private int missioniConSparo;
    private int missioniSenzaSparo;
    private BigDecimal oreVoloTotali;
    private double vitaResiduaPercentuale;

    /**
     * Default constructor.
     */
    public LauncherLifeStatus() {
    }

    /**
     * Constructor with parameters.
     *
     * @param nomeLanciatore Launcher name
     * @param partNumber Part number
     * @param serialNumber Serial number
     * @param numeroMissioni Number of missions
     * @param missioniConSparo Number of missions with firing
     * @param missioniSenzaSparo Number of missions without firing
     * @param oreVoloTotali Total flight hours
     * @param vitaResiduaPercentuale Residual life percentage
     */
    public LauncherLifeStatus(String nomeLanciatore, String partNumber, String serialNumber,
                              int numeroMissioni, int missioniConSparo, int missioniSenzaSparo,
                              BigDecimal oreVoloTotali, double vitaResiduaPercentuale) {
        this.nomeLanciatore = nomeLanciatore;
        this.partNumber = partNumber;
        this.serialNumber = serialNumber;
        this.numeroMissioni = numeroMissioni;
        this.missioniConSparo = missioniConSparo;
        this.missioniSenzaSparo = missioniSenzaSparo;
        this.oreVoloTotali = oreVoloTotali;
        this.vitaResiduaPercentuale = vitaResiduaPercentuale;
    }

    /**
     * Gets the launcher name.
     *
     * @return The launcher name
     */
    public String getNomeLanciatore() {
        return nomeLanciatore;
    }

    /**
     * Sets the launcher name.
     *
     * @param nomeLanciatore The launcher name to set
     */
    public void setNomeLanciatore(String nomeLanciatore) {
        this.nomeLanciatore = nomeLanciatore;
    }

    /**
     * Gets the part number.
     *
     * @return The part number
     */
    public String getPartNumber() {
        return partNumber;
    }

    /**
     * Sets the part number.
     *
     * @param partNumber The part number to set
     */
    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    /**
     * Gets the serial number.
     *
     * @return The serial number
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Sets the serial number.
     *
     * @param serialNumber The serial number to set
     */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * Gets the number of missions.
     *
     * @return The number of missions
     */
    public int getNumeroMissioni() {
        return numeroMissioni;
    }

    /**
     * Sets the number of missions.
     *
     * @param numeroMissioni The number of missions to set
     */
    public void setNumeroMissioni(int numeroMissioni) {
        this.numeroMissioni = numeroMissioni;
    }

    /**
     * Gets the number of missions with firing.
     *
     * @return The number of missions with firing
     */
    public int getMissioniConSparo() {
        return missioniConSparo;
    }

    /**
     * Sets the number of missions with firing.
     *
     * @param missioniConSparo The number of missions with firing to set
     */
    public void setMissioniConSparo(int missioniConSparo) {
        this.missioniConSparo = missioniConSparo;
    }

    /**
     * Gets the number of missions without firing.
     *
     * @return The number of missions without firing
     */
    public int getMissioniSenzaSparo() {
        return missioniSenzaSparo;
    }

    /**
     * Sets the number of missions without firing.
     *
     * @param missioniSenzaSparo The number of missions without firing to set
     */
    public void setMissioniSenzaSparo(int missioniSenzaSparo) {
        this.missioniSenzaSparo = missioniSenzaSparo;
    }

    /**
     * Gets the total flight hours.
     *
     * @return The total flight hours
     */
    public BigDecimal getOreVoloTotali() {
        return oreVoloTotali;
    }

    /**
     * Sets the total flight hours.
     *
     * @param oreVoloTotali The total flight hours to set
     */
    public void setOreVoloTotali(BigDecimal oreVoloTotali) {
        this.oreVoloTotali = oreVoloTotali;
    }

    /**
     * Gets the residual life percentage.
     *
     * @return The residual life percentage
     */
    public double getVitaResiduaPercentuale() {
        return vitaResiduaPercentuale;
    }

    /**
     * Sets the residual life percentage.
     *
     * @param vitaResiduaPercentuale The residual life percentage to set
     */
    public void setVitaResiduaPercentuale(double vitaResiduaPercentuale) {
        this.vitaResiduaPercentuale = vitaResiduaPercentuale;
    }

    /**
     * Returns a string representation of the LauncherLifeStatus object.
     *
     * @return A string representation of the LauncherLifeStatus
     */
    @Override
    public String toString() {
        return "LauncherLifeStatus{" +
                "nomeLanciatore='" + nomeLanciatore + '\'' +
                ", partNumber='" + partNumber + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", numeroMissioni=" + numeroMissioni +
                ", missioniConSparo=" + missioniConSparo +
                ", missioniSenzaSparo=" + missioniSenzaSparo +
                ", oreVoloTotali=" + oreVoloTotali +
                ", vitaResiduaPercentuale=" + vitaResiduaPercentuale +
                '}';
    }
}