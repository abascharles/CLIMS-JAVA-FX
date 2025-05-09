package com.aircraft.model;

/**
 * Model class representing an aircraft in the system.
 * Corresponds to the 'matricola_velivolo' table in the database.
 */
public class Aircraft {
    private String matricolaVelivolo;

    /**
     * Default constructor.
     */
    public Aircraft() {
    }

    /**
     * Constructor with parameter.
     *
     * @param matricolaVelivolo Aircraft serial number (primary key)
     */
    public Aircraft(String matricolaVelivolo) {
        this.matricolaVelivolo = matricolaVelivolo;
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
     * Returns a string representation of the Aircraft object.
     *
     * @return A string representation of the Aircraft
     */
    @Override
    public String toString() {
        return "Aircraft{" +
                "matricolaVelivolo='" + matricolaVelivolo + '\'' +
                '}';
    }
}