package com.aircraft.model;

import java.sql.Date;
import java.sql.Time;

/**
 * Model class representing a mission in the system.
 * Corresponds to the 'missione' table in the database.
 */
public class Mission {
    private int id;
    private String matricolaVelivolo;
    private Date dataMissione;
    private int numeroVolo;
    private Time oraPartenza;
    private Time oraArrivo;

    /**
     * Default constructor.
     */
    public Mission() {
    }

    /**
     * Constructor with parameters.
     *
     * @param id Mission ID
     * @param matricolaVelivolo Aircraft serial number
     * @param dataMissione Mission date
     * @param numeroVolo Flight number
     * @param oraPartenza Departure time
     * @param oraArrivo Arrival time
     */
    public Mission(int id, String matricolaVelivolo, Date dataMissione, int numeroVolo, Time oraPartenza, Time oraArrivo) {
        this.id = id;
        this.matricolaVelivolo = matricolaVelivolo;
        this.dataMissione = dataMissione;
        this.numeroVolo = numeroVolo;
        this.oraPartenza = oraPartenza;
        this.oraArrivo = oraArrivo;
    }

    /**
     * Gets the mission ID.
     *
     * @return The mission ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the mission ID.
     *
     * @param id The mission ID to set
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
     * Gets the mission date.
     *
     * @return The mission date
     */
    public Date getDataMissione() {
        return dataMissione;
    }

    /**
     * Sets the mission date.
     *
     * @param dataMissione The mission date to set
     */
    public void setDataMissione(Date dataMissione) {
        this.dataMissione = dataMissione;
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
     * Gets the departure time.
     *
     * @return The departure time
     */
    public Time getOraPartenza() {
        return oraPartenza;
    }

    /**
     * Sets the departure time.
     *
     * @param oraPartenza The departure time to set
     */
    public void setOraPartenza(Time oraPartenza) {
        this.oraPartenza = oraPartenza;
    }

    /**
     * Gets the arrival time.
     *
     * @return The arrival time
     */
    public Time getOraArrivo() {
        return oraArrivo;
    }

    /**
     * Sets the arrival time.
     *
     * @param oraArrivo The arrival time to set
     */
    public void setOraArrivo(Time oraArrivo) {
        this.oraArrivo = oraArrivo;
    }

    /**
     * Returns a string representation of the Mission object.
     *
     * @return A string representation of the Mission
     */
    @Override
    public String toString() {
        return "Mission{" +
                "id=" + id +
                ", matricolaVelivolo='" + matricolaVelivolo + '\'' +
                ", dataMissione=" + dataMissione +
                ", numeroVolo=" + numeroVolo +
                ", oraPartenza=" + oraPartenza +
                ", oraArrivo=" + oraArrivo +
                '}';
    }
}