package com.aircraft.model;

import java.math.BigDecimal;

/**
 * Model class representing a weapon in the system.
 * Corresponds to the 'anagrafica_carichi' table in the database.
 */
public class Weapon {
    private String partNumber;
    private String nomenclatura;
    private String codiceDitta;
    private BigDecimal massa;

    /**
     * Default constructor.
     */
    public Weapon() {
    }

    /**
     * Constructor with parameters.
     *
     * @param partNumber Part number (primary key)
     * @param nomenclatura Nomenclature/Name
     * @param codiceDitta Company code
     * @param massa Mass in kg
     */
    public Weapon(String partNumber, String nomenclatura, String codiceDitta, BigDecimal massa) {
        this.partNumber = partNumber;
        this.nomenclatura = nomenclatura;
        this.codiceDitta = codiceDitta;
        this.massa = massa;
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
     * Gets the nomenclature (name).
     *
     * @return The nomenclature
     */
    public String getNomenclatura() {
        return nomenclatura;
    }

    /**
     * Sets the nomenclature (name).
     *
     * @param nomenclatura The nomenclature to set
     */
    public void setNomenclatura(String nomenclatura) {
        this.nomenclatura = nomenclatura;
    }

    /**
     * Gets the company code.
     *
     * @return The company code
     */
    public String getCodiceDitta() {
        return codiceDitta;
    }

    /**
     * Sets the company code.
     *
     * @param codiceDitta The company code to set
     */
    public void setCodiceDitta(String codiceDitta) {
        this.codiceDitta = codiceDitta;
    }

    /**
     * Gets the mass in kg.
     *
     * @return The mass
     */
    public BigDecimal getMassa() {
        return massa;
    }

    /**
     * Sets the mass in kg.
     *
     * @param massa The mass to set
     */
    public void setMassa(BigDecimal massa) {
        this.massa = massa;
    }

    /**
     * Returns a string representation of the Weapon object.
     *
     * @return A string representation of the Weapon
     */
    @Override
    public String toString() {
        return "Weapon{" +
                "partNumber='" + partNumber + '\'' +
                ", nomenclatura='" + nomenclatura + '\'' +
                ", codiceDitta='" + codiceDitta + '\'' +
                ", massa=" + massa +
                '}';
    }
}