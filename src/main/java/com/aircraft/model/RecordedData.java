package com.aircraft.model;

import java.math.BigDecimal;

/**
 * Model class representing recorded flight data in the system.
 * Corresponds to the 'dati_registrati' table in the database.
 */
public class RecordedData {
    private int id;
    private String matricolaVelivolo;
    private int numeroVolo;
    private BigDecimal gloadMax;
    private BigDecimal gloadMin;
    private Integer quotaMedia;
    private Integer velocitaMassima;
    private String statoMissili;
    private boolean statoElaborato;

    /**
     * Default constructor.
     */
    public RecordedData() {
    }

    /**
     * Constructor with parameters.
     *
     * @param id Record ID (primary key)
     * @param matricolaVelivolo Aircraft serial number
     * @param numeroVolo Flight number
     * @param gloadMax Maximum G-load
     * @param gloadMin Minimum G-load
     * @param quotaMedia Average altitude
     * @param velocitaMassima Maximum speed
     * @param statoMissili Missile status string
     * @param statoElaborato Processed status flag
     */
    public RecordedData(int id, String matricolaVelivolo, int numeroVolo, BigDecimal gloadMax,
                        BigDecimal gloadMin, Integer quotaMedia, Integer velocitaMassima,
                        String statoMissili, boolean statoElaborato) {
        this.id = id;
        this.matricolaVelivolo = matricolaVelivolo;
        this.numeroVolo = numeroVolo;
        this.gloadMax = gloadMax;
        this.gloadMin = gloadMin;
        this.quotaMedia = quotaMedia;
        this.velocitaMassima = velocitaMassima;
        this.statoMissili = statoMissili;
        this.statoElaborato = statoElaborato;
    }

    /**
     * Gets the record ID.
     *
     * @return The record ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the record ID.
     *
     * @param id The record ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the aircraft serial number.
     *
     * @return The aircraft serial number
     */
    public String getMatricolaVelivolo() {
        return matricolaVelivolo;
    }

    /**
     * Sets the aircraft serial number.
     *
     * @param matricolaVelivolo The aircraft serial number to set
     */
    public void setMatricolaVelivolo(String matricolaVelivolo) {
        this.matricolaVelivolo = matricolaVelivolo;
    }

    /**
     * Gets the flight number.
     *
     * @return The flight number
     */
    public int getNumeroVolo() {
        return numeroVolo;
    }

    /**
     * Sets the flight number.
     *
     * @param numeroVolo The flight number to set
     */
    public void setNumeroVolo(int numeroVolo) {
        this.numeroVolo = numeroVolo;
    }

    /**
     * Gets the maximum G-load.
     *
     * @return The maximum G-load
     */
    public BigDecimal getGloadMax() {
        return gloadMax;
    }

    /**
     * Sets the maximum G-load.
     *
     * @param gloadMax The maximum G-load to set
     */
    public void setGloadMax(BigDecimal gloadMax) {
        this.gloadMax = gloadMax;
    }

    /**
     * Gets the minimum G-load.
     *
     * @return The minimum G-load
     */
    public BigDecimal getGloadMin() {
        return gloadMin;
    }

    /**
     * Sets the minimum G-load.
     *
     * @param gloadMin The minimum G-load to set
     */
    public void setGloadMin(BigDecimal gloadMin) {
        this.gloadMin = gloadMin;
    }

    /**
     * Gets the average altitude.
     *
     * @return The average altitude
     */
    public Integer getQuotaMedia() {
        return quotaMedia;
    }

    /**
     * Sets the average altitude.
     *
     * @param quotaMedia The average altitude to set
     */
    public void setQuotaMedia(Integer quotaMedia) {
        this.quotaMedia = quotaMedia;
    }

    /**
     * Gets the maximum speed.
     *
     * @return The maximum speed
     */
    public Integer getVelocitaMassima() {
        return velocitaMassima;
    }

    /**
     * Sets the maximum speed.
     *
     * @param velocitaMassima The maximum speed to set
     */
    public void setVelocitaMassima(Integer velocitaMassima) {
        this.velocitaMassima = velocitaMassima;
    }

    /**
     * Gets the missile status string.
     *
     * @return The missile status string
     */
    public String getStatoMissili() {
        return statoMissili;
    }

    /**
     * Sets the missile status string.
     *
     * @param statoMissili The missile status string to set
     */
    public void setStatoMissili(String statoMissili) {
        this.statoMissili = statoMissili;
    }

    /**
     * Gets the processed status flag.
     *
     * @return The processed status flag
     */
    public boolean isStatoElaborato() {
        return statoElaborato;
    }

    /**
     * Sets the processed status flag.
     *
     * @param statoElaborato The processed status flag to set
     */
    public void setStatoElaborato(boolean statoElaborato) {
        this.statoElaborato = statoElaborato;
    }

    /**
     * Returns a string representation of the RecordedData object.
     *
     * @return A string representation of the RecordedData
     */
    @Override
    public String toString() {
        return "RecordedData{" +
                "id=" + id +
                ", matricolaVelivolo='" + matricolaVelivolo + '\'' +
                ", numeroVolo=" + numeroVolo +
                ", gloadMax=" + gloadMax +
                ", gloadMin=" + gloadMin +
                ", quotaMedia=" + quotaMedia +
                ", velocitaMassima=" + velocitaMassima +
                ", statoMissili='" + statoMissili + '\'' +
                ", statoElaborato=" + statoElaborato +
                '}';
    }
}